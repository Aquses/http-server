package ass2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

public class HttpResponse {
    String protocol;
    StatusCode status;
    String body;
    // private RequestMethod requestMethod;
    private HttpHeader header;

    public HttpResponse(String protocol, StatusCode code, HttpHeader headers, Mime contentType, String body) {
        this.protocol = protocol;
        this.status = code;
        this.header = headers;
        this.body = body;

        if (contentType != null) {
            var a = (contentType.getValue());
            headers.add("Content-Type", a); // + "; charset=UTF-8"
        }

        if (this.body != null) {
            byte[] bodyBytes = this.body.getBytes(StandardCharsets.UTF_8);
            headers.add("Content-Length", Integer.toString(bodyBytes.length));
        }
    }

    public String getBody() {
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

    public void writeTo(BufferedWriter out) throws IOException {
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append(this.getProtocol()).append(" ").append(getStatusCode()).append("\r\n");

        for (String headerLine : this.getHeaders()) {
            responseBuilder.append(headerLine).append("\r\n");
        }
        responseBuilder.append("\r\n");

        if (this.body != null) {
            responseBuilder.append(this.body);
        }
        var result = responseBuilder.toString();
        out.write(result);
    }

    static public void printHttpResponse(HttpResponse response) {
        System.out.println("HTTP Response: ");
        System.out.println(" Method: " + response.getProtocol());
        System.out.println(" Status: " + response.getStatusCode().toString());
        String date = " Date: " + new Date();

        String[] headers = response.getHeaders();
        for (String header : headers) {
            System.out.println("  " + header);
        }
    }
}
