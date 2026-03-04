package co.edu.escuelaing.reflexionlab;

import co.edu.escuelaing.reflexionlab.annotations.GetMapping;
import co.edu.escuelaing.reflexionlab.annotations.RequestParam;
import co.edu.escuelaing.reflexionlab.annotations.RestController;
import co.edu.escuelaing.reflexionlab.server.HttpServer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.*;

public class MicroSpringBoot {

    public static void main(String[] args) throws Exception {
        int port = 8080;
        HttpServer server = new HttpServer(port);

        // 1. Scan for components
        List<Class<?>> componentClasses = new ArrayList<>();

        // Scan package "co.edu.escuelaing.reflexionlab"
        String basePackage = "co.edu.escuelaing.reflexionlab";
        List<Class<?>> classes = getClasses(basePackage);

        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(RestController.class)) {
                componentClasses.add(clazz);
                System.out.println("Loaded component: " + clazz.getName());
            }
        }

        // 2. Register routes
        for (Class<?> clazz : componentClasses) {
            Object instance = clazz.getDeclaredConstructor().newInstance();

            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(GetMapping.class)) {
                    GetMapping getMapping = method.getAnnotation(GetMapping.class);
                    String path = getMapping.value();

                    System.out.println("Mapped route: " + path + " -> " + clazz.getName() + "." + method.getName());

                    server.addRoute(path, queryParams -> {
                        return (String) invokeMethod(instance, method, queryParams);
                    });
                }
            }
        }

        // 3. Start server
        server.start();
    }

    private static Object invokeMethod(Object instance, Method method, Map<String, String> queryParams)
            throws Exception {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            if (param.isAnnotationPresent(RequestParam.class)) {
                RequestParam rp = param.getAnnotation(RequestParam.class);
                String paramName = rp.value();
                String defaultValue = rp.defaultValue();

                String value = queryParams.getOrDefault(paramName, "");
                if (value.isEmpty() && !defaultValue.isEmpty()) {
                    value = defaultValue;
                }

                args[i] = value;
            } else {
                args[i] = null;
            }
        }

        return method.invoke(instance, args);
    }

    private static List<Class<?>> getClasses(String packageName) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        List<Class<?>> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes;
    }

    private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        if (files == null) {
            return classes;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(
                        Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }
}
