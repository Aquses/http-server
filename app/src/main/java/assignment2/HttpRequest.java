package assignment2;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

public class HttpRequest {
    private RequestMethod requestMethod;
    private HttpHeader header;
    private String body;
    private URI uri;
    private String protocol;

    public static Optional<HttpRequest> parse(BufferedReader requestStream) throws IOException, Exception {
        StringBuilder sh = new StringBuilder();
        StringBuilder sb = new StringBuilder();

        String line;

        while ((line = requestStream.readLine()) != null && !line.isEmpty()) {
            sh.append(line).append("\r\n");
        }

        try {
            if (requestStream.ready()) {
                char[] buffer = new char[1024];
                int bytesRead;

                    while (requestStream.ready() && (bytesRead = requestStream.read(buffer)) != -1) {
                        sb.append(buffer, 0, bytesRead);
                    }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (sh.length() == 0) {
            return Optional.empty();
        }

        String rawRequest = sh.toString();
        String[] lines = rawRequest.split("\\r?\\n");
        if (lines.length < 3) {
            throw new Exception("Invalid HTTP request format");
        }

        String[] requestLine = lines[0].split("\\s");
        if (requestLine.length < 3) {
            throw new Exception("Invalid HTTP request line format");
        }
        var request = new HttpRequest();
        request.requestMethod = RequestMethod.valueOf(requestLine[0]);
        request.uri = URI.create(requestLine[1]);
        request.protocol = requestLine[2];
        request.header = new HttpHeader(lines, 1);
        request.body = sb.toString();
        return Optional.of(request);

    }

    public RequestMethod getRequestMethod() {
        return requestMethod;
    }

    public String getBody() {
        return body;
    }

    public URI getUri() {
        return uri;
    }

    public String getProtocol() {
        return protocol;
    }

    public HttpHeader getHeader() {
        return this.header;
    }

    public void print() {
        System.out.println("Request");
        System.out.println("Request Method: " + requestMethod);
        System.out.println("URI: " + uri);
        System.out.println("Protocol: " + protocol);
        System.out.println("Headers:");
        for (Map.Entry<String, String> entry : header.headers.entrySet()) {
            String headerName = entry.getKey();
            var headerValues = entry.getValue();
            System.out.println(headerName + ": " + headerValues);
        }
        System.out.println("Body:");
        System.out.println(body);
    }

}
