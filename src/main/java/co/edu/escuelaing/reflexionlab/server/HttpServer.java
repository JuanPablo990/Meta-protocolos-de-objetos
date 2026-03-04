package co.edu.escuelaing.reflexionlab.server;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class HttpServer {
    private final int port;
    private final Map<String, RouteHandler> apiRoutes;

    public interface RouteHandler {
        String handle(Map<String, String> queryParams) throws Exception;
    }

    public HttpServer(int port) {
        this.port = port;
        this.apiRoutes = new HashMap<>();
    }

    public void addRoute(String path, RouteHandler handler) {
        apiRoutes.put(path, handler);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("MicroSpringBoot Server started on port " + port);
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    handleClient(clientSocket);
                } catch (IOException e) {
                    System.err.println("Error handling client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + port);
        }
    }

    private void handleClient(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        OutputStream out = clientSocket.getOutputStream();

        String requestLine = in.readLine();
        if (requestLine == null)
            return;

        System.out.println("Request: " + requestLine);
        String[] tokens = requestLine.split(" ");
        if (tokens.length >= 2 && tokens[0].equals("GET")) {
            String uri = tokens[1];

            // Read headers (we ignore them for now but need to read them to empty the
            // buffer)
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.isEmpty())
                    break;
            }

            String path = uri;
            String query = "";
            if (uri.contains("?")) {
                int qIndex = uri.indexOf("?");
                path = uri.substring(0, qIndex);
                query = uri.substring(qIndex + 1);
            }

            Map<String, String> queryParams = parseQueryParams(query);

            if (apiRoutes.containsKey(path)) {
                try {
                    String responseBody = apiRoutes.get(path).handle(queryParams);
                    sendHttpResponse(out, "200 OK", "text/plain", responseBody.getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                    sendHttpResponse(out, "500 Internal Server Error", "text/plain", e.toString().getBytes());
                }
            } else {
                serveStaticFile(out, path);
            }
        } else {
            // Read headers
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.isEmpty())
                    break;
            }
            sendHttpResponse(out, "405 Method Not Allowed", "text/plain", "Method Not Allowed".getBytes());
        }

        out.close();
        in.close();
        clientSocket.close();
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty())
            return params;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                params.put(kv[0], kv[1]);
            } else if (kv.length == 1) {
                params.put(kv[0], "");
            }
        }
        return params;
    }

    private void serveStaticFile(OutputStream out, String path) throws IOException {
        if (path.equals("/")) {
            path = "/index.html";
        }

        Path filePath = Paths.get("target/classes/public" + path);

        if (!Files.exists(filePath)) {
            filePath = Paths.get("src/main/resources/public" + path);
        }

        if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
            String contentType = getContentType(path);
            byte[] fileBytes = Files.readAllBytes(filePath);
            sendHttpResponse(out, "200 OK", contentType, fileBytes);
        } else {
            sendHttpResponse(out, "404 Not Found", "text/plain", "404 Not Found".getBytes());
        }
    }

    private String getContentType(String path) {
        if (path.endsWith(".html"))
            return "text/html";
        if (path.endsWith(".png"))
            return "image/png";
        if (path.endsWith(".css"))
            return "text/css";
        if (path.endsWith(".js"))
            return "application/javascript";
        return "text/plain";
    }

    private void sendHttpResponse(OutputStream out, String status, String contentType, byte[] body) throws IOException {
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 ").append(status).append("\r\n");
        response.append("Content-Type: ").append(contentType).append("\r\n");
        response.append("Content-Length: ").append(body.length).append("\r\n");
        response.append("\r\n");
        out.write(response.toString().getBytes());
        out.write(body);
        out.flush();
    }
}
