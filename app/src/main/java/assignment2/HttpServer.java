package assignment2;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        try (var inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                var outputStream = clientSocket.getOutputStream())
        {
            try {
                // new BufferedWriter
                var request = HttpRequest.parse(inputStream);
             
                if(request.isEmpty()) {
                    return; 
                }
                Optional<HttpResponse> response = request_handler(request.get());

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
        request.print();
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
                
                    Mime contentType = getMimeFromFile(resourceFile);
                    try (var inputStream = new FileInputStream(resourceFile)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            byteArrayOutputStream.write(buffer, 0, bytesRead);
                        }
                        return Optional.of(new HttpResponse("HTTP/1.1", StatusCode.OK,
                        new HttpHeader(), contentType, byteArrayOutputStream.toByteArray()));

                    } catch (IOException e) {
                        e.printStackTrace();
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
                        if (request.getHeader().getContentType().isPresent()) {
                            var contentType = request.getHeader().getContentType().get();
                            if (contentType.equals(Mime.APPLICATION_X_WWW_FORM_URLENCODED)) {
                                Credentials credentials = Credentials.loadCredentials();
                                var body = request.getBody();
                                if (body != null && !body.isEmpty()) {
                                    Map<String, String> parameters = new HashMap<String, String>();
                                    String[] pairs = body.split("&");
                                    for (String pair : pairs) {
                                        String[] keyValue = pair.split("=");
                                        if (keyValue.length == 2) {
                                            String key = URLDecoder.decode(keyValue[0],
                                                    StandardCharsets.UTF_8.toString());
                                            String value = URLDecoder.decode(keyValue[1],
                                                    StandardCharsets.UTF_8.toString());
                                            parameters.put(key, value);
                                        }
                                    }
                                    String username = parameters.get("username");
                                    String password = parameters.get("password");
                                    System.out.println("username" + username);
                                    if ((username.equals(credentials.username) )
                                            && (password.equals(credentials.password))) {
                                        return Optional.of(new HttpResponse("HTTP/1.1", StatusCode.OK,
                                                new HttpHeader(), Mime.TXT, "Login successful"));
                                    } else {
                                        return Optional.of(new HttpResponse("HTTP/1.1", StatusCode.UNAUTHORIZED,
                                                new HttpHeader(), Mime.TXT, "Unauthorized"));
                                    }
                                }
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        return Optional.of(new HttpResponse("HTTP/1.1", StatusCode.INTERNAL_SERVER_ERROR,
                                new HttpHeader(), Mime.TXT, "500 Internal Server Error it goes through"));
                    }
                }
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
}