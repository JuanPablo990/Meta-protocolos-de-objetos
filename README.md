# Taller de Arquitecturas: Meta Protocolos de Objetos, Servidor Web e IoC Framework

Un servidor Web ligero en Java (tipo Apache) construido desde cero utilizando Sockets TCP nativos, que implementa un Framework de Inversión de Control (IoC) basado en la manipulación de Meta Protocolos mediante la API de Reflexión de Java.

## Descripción

El proyecto consiste en dos componentes principales:
1. **Servidor HTTP:** Capaz de recibir y procesar peticiones web. Puede entregar de forma nativa recursos estáticos (imágenes PNG, páginas HTML, CSS, JS) almacenados en la carpeta `src/main/resources/public`.
2. **MicroSpringBoot (Framework IoC):** Un motor reflexivo que escanea el _classpath_ del proyecto durante su inicialización buscando clases marcadas como componentes (`@RestController`). Utiliza reflexión dinámica para registrar los métodos anotados (`@GetMapping`) a diferentes rutas URI y posteriormente invocarlos a solicitud del servidor inyectando valores mapeados desde la petición HTTP (`@RequestParam`).

## Requisitos Previos

Para ejecutar y compilar este proyecto, necesitas tener instalado:

- Java Development Kit (JDK) 8 o superior.
- Apache Maven (Gestor de dependencias y empaquetado).
- Git (Opcional, para el clonado del repositorio).

## Instalación y Ejecución

Sigue estos pasos para compilar y probar la aplicación en tu entorno de desarrollo local.

1. **Clona o descarga el repositorio** en tu máquina local:
   ```bash
   git clone <url-de-tu-repositorio>
   cd Meta-protocolos-de-objetos
   ```

2. **Compila el proyecto con Maven**. Este comando descargará requerimientos, limpiará entregas anteriores y compilará las clases de Java:
   ```bash
   mvn clean compile
   ```

3. **Inicia el Servidor de Aplicaciones**. Emplea la JVM para arrancar la clase central del framework:
   ```bash
   java -cp target/classes co.edu.escuelaing.reflexionlab.MicroSpringBoot
   ```

Verás una salida en consola indicando que se han cargado los componentes y que el servidor de *MicroSpringBoot* ha iniciado exitosamente en el puerto `8080`.

## Guía de Uso Rápida

Con el servidor encendido, ingresa a tu navegador web o utiliza herramientas de terminal como `curl` para solicitar las respuestas de los controladores de prueba predeterminados:

- **Retorno String básico:**
  Navega hacia `http://localhost:8080/hello`
  > Retornará: `Greetings from Spring Boot!`

- **Uso de Parámetros URI (Inyección Query String):**
  Navega hacia `http://localhost:8080/greeting?name=PruebaLocal`
  > Retornará: `Hola PruebaLocal`

- **Uso de Valores por Defecto:**
  Navega hacia `http://localhost:8080/greeting` sin parámetros
  > Retornará el default value de la anotación: `Hola World`

- **Visualización de Archivos Estáticos:**
  Navega hacia `http://localhost:8080/index.html`
  > Entregará la página web ubicada dentro de `resources/public`.

## Construido Con

*   **Java (JDK)** - Lenguaje y base lógica. API Reflection.
*   **Java Sockets** - API Nativa para concurrencia básica y peticiones web (`java.net.ServerSocket`).
*   **Apache Maven** - Dependencias, compilación y empaquetado del ciclo de vida.

## Autores

*   **Juan Pablo Nieto Cortes** - *Trabajo y desarrollo completo* - (https://github.com/JuanPablo990)

## Licencia

Este proyecto está licenciado bajo la **MIT License** - vea el archivo [LICENSE](LICENSE) para más detalles.