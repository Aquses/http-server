package ass2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
// import org.json.simple.JSONObject; 

public class HttpServer {
    private static final Logger logger = Logger.getLogger(HttpServer.class.getName());
    private String rootDirectory;
    private ServerSocket serverSocket;
    private ExecutorService executorService;

    public HttpServer(String rootDirectory) {
        this.rootDirectory = rootDirectory;
        this.executorService = Executors.newFixedThreadPool(10); // Adjust the pool size as needed
    }

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port, 10);
            logger.log(Level.INFO, "Server started\nListening for messages from port: " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                executorService.execute(() -> handleClientRequest(clientSocket));
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error starting the server", ex);
        }
    }

    private void handleClientRequest(Socket clientSocket) {
        try (BufferedReader inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                BufferedWriter outputStream = new BufferedWriter(
                        new OutputStreamWriter(clientSocket.getOutputStream()))) {
            try {
                HttpRequest request = new HttpRequest(inputStream);
                Optional<HttpResponse> response = request_handler(request);

                if (response.isPresent()) {
                    HttpResponse.printHttpResponse(response.get());
                    response.get().writeTo(outputStream);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error handling client request", e);
        } finally {
            closeResources(clientSocket);
        }
    }

    public Optional<HttpResponse> request_handler(HttpRequest request) throws Exception {
        switch (request.getRequestMethod()) {
            case GET: {
                try {
                    URI uri = request.getUri();
                    File resourceFile = getResousFile(uri);


                    if (resourceFile.isDirectory()) {
                        resourceFile = new File(resourceFile, "index.html");
                    }

                    if (!resourceFile.exists()) {
                        return Optional.of(new HttpResponse("HTTP/1.1", StatusCode.NOT_FOUND,
                                new HttpHeader(), Mime.TXT, "404 Not Found"));
                    }

                    // // ||||| LOGIN ||||||
                    // if ("login.html".equals(request.getUri().getPath())) {

                    // }

                    Mime contentType = getMimeFromFile(resourceFile);
                    try (BufferedReader reader = new BufferedReader(new FileReader(resourceFile))) {
                        StringBuilder content = new StringBuilder();
                        String line;

                        while ((line = reader.readLine()) != null) {
                            content.append(line);
                        }

                        return Optional.of(new HttpResponse("HTTP/1.1", StatusCode.OK,
                                new HttpHeader(), contentType, content.toString()));
                    } catch (IOException e) {
                        return Optional.of(new HttpResponse("HTTP/1.1", StatusCode.INTERNAL_SERVER_ERROR,
                                new HttpHeader(), Mime.TXT, "500 Internal Server Error"));
                    }
                } catch (IOException ex) {
                    return Optional.of(new HttpResponse("HTTP/1.1", StatusCode.INTERNAL_SERVER_ERROR,
                            new HttpHeader(), Mime.TXT, "500 Internal Server Error"));
                }
            }
            case POST: {
                // ||||| LOGIN ||||||
                if ("/login".equals(request.getUri().getPath())) {
                    try {
                        // Read the submitted credentials from the request
                        String submittedUsername = request.getParameters().getOrDefault("username", "");
                        String submittedPassword = request.getParameters().getOrDefault("password", "");
                        Map<String, String> storedCredentials = request.getCredentials();

                        // DEBUG
                        System.out.println("Stored Credentials: " + storedCredentials);
                        System.out.println("Submitted Username: " + submittedUsername);
                        System.out.println("Submitted Password: " + submittedPassword);

                        if (storedCredentials.containsKey(submittedUsername) && storedCredentials.get(submittedUsername).equals(submittedPassword)) {
                            return Optional.of(new HttpResponse("HTTP/1.1", StatusCode.OK,
                                new HttpHeader(), Mime.TXT, "Login successful"));
                        } else {
                            return Optional.of(new HttpResponse("HTTP/1.1", StatusCode.UNAUTHORIZED,
                                    new HttpHeader(), Mime.TXT, "Unauthorized"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.of(new HttpResponse("HTTP/1.1", StatusCode.INTERNAL_SERVER_ERROR,
                                new HttpHeader(), Mime.TXT, "500 Internal Server Error it goes through"));
                    }
                }

                // if ("upload.html".equals(request.getUri().getPath())) {
                    
                // }


            }
            default:
                System.err.println("Unhandled request: " + request.getRequestMethod() + " " + request.getUri());
                return Optional.of(new HttpResponse("HTTP/1.1", StatusCode.INTERNAL_SERVER_ERROR,
                        new HttpHeader(), Mime.TXT, "Default: 500 Internal Server Error"));
        }
    }

    private void closeResources(Socket clientSocket) {
        try {
            clientSocket.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error closing client socket", e);
        }
    }

    public void stop() {
        executorService.shutdown();
        try {
            serverSocket.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error closing server socket", e);
        }
    }

    public Optional<String> getExtensionByStringHandling(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }

    public Mime getMimeFromFile(File file) {
        var fileName = file.getName();
        var parts = fileName.split("\\.");

        if (parts.length > 1) {
            return Mime.getByFileExtension(parts[parts.length - 1]);
        }

        return Mime.TXT;
    }

    public File getResousFile(URI uri) throws FileNotFoundException {
        String path = uri.getPath();
        File file = new File(rootDirectory, path);

        if (!file.exists()) {
            file = new File(rootDirectory, path + ".html");
        }
        return file;
    }

    public BufferedReader readResource(File file) throws FileNotFoundException {
        try {
            return new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw e;
        }
    }


    public static void main(String[] args) {
        int port = 80;

        // if (args.length != 2) {
        // System.err.println("Usage: <portnumber> <public>");
        // System.exit(1);
        // }

        // try {
        // port = Integer.parseInt(args[0]);
        // if (port < 0 || port >= 65535) {
        // System.err.println("The port needs to be in the range of 0-65535");
        // System.exit(1);
        // }
        // } catch (NumberFormatException ex) {
        // System.err.println("The port must be an integer.");
        // System.exit(1);
        // return;
        // }

        String rootDir = "public";
        // String rootDir = args[1];
        Path path = Path.of(System.getProperty("user.dir"), rootDir);
        if (!Files.isDirectory(path)) {
            System.err.println("The directory \"" + path.toString() + "\" does not exist.");
            System.exit(1);
        }
        HttpServer server = new HttpServer(path.toString());
        try {
            server.start(port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}