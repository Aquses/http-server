package assignment2;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HttpResponse {
    String protocol;
    StatusCode status;
    byte[] body;
    // private RequestMethod requestMethod;
    private HttpHeader header;

    public HttpResponse(String protocol, StatusCode code, HttpHeader headers, Mime contentType, byte[] body) {
        this.protocol = protocol;
        this.status = code;
        this.header = headers;
        this.body = body;

        if (contentType != null) {
            var a = (contentType.getValue());
            headers.add("Content-Type", a); // + "; charset=UTF-8"
        }

        if (this.body != null) {
            // byte[] bodyBytes = this.body.getBytes(StandardCharsets.UTF_8);
            // headers.add("Content-Length", Integer.toString(bodyBytes.length));
            header.add("Content-Length", Integer.toString(body.length));
        }
    }

    public HttpResponse(String protocol, StatusCode code, HttpHeader headers, Mime contentType, String body) {
        this.protocol = protocol;
        this.status = code;
        this.header = headers;
        this.body = body.getBytes();

        if (contentType != null) {
            var a = (contentType.getValue());
            headers.add("Content-Type", a); // + "; charset=UTF-8"
        }

        if (this.body != null) {
            // byte[] bodyBytes = this.body.getBytes(StandardCharsets.UTF_8);
            // headers.add("Content-Length", Integer.toString(bodyBytes.length));
            header.add("Content-Length", Integer.toString(this.body.length));
        }
    }

    public byte[] getBody() {
        return body;
    }

    public String getProtocol() {
        return protocol;
    }

    public StatusCode getStatusCode() {
        return status;
    }

    public String[] getHeaders() {
        return this.header.getHeaders();
    }
 //BufferedWriter
    public void writeTo(OutputStream out) throws IOException {
        
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append(this.getProtocol()).append(" ").append(getStatusCode()).append("\r\n");
        for (String headerLine : this.getHeaders()) {
            responseBuilder.append(headerLine).append("\r\n");
        }
        responseBuilder.append("\r\n");
        var result = responseBuilder.toString();
        out.write(result.getBytes());
        
        var contentType = this.header.getContentType();
        if (contentType.isPresent()) {
            Mime mime = this.header.getContentType().get();
            switch (mime) {
                case PNG:
                    out.write(this.body);
                    break;
                default:
                    out.write(new String(this.body, StandardCharsets.UTF_8).getBytes());
                    break;
            }
        }

    }

    static public void printHttpResponse(HttpResponse response) {
        System.out.println("HTTP Response: ");
        System.out.println(" Method: " + response.getProtocol());
        System.out.println(" Status: " + response.getStatusCode().toString());

        String[] headers = response.getHeaders();
        for (String header : headers) {
            System.out.println("  " + header);
        }
    }
}
