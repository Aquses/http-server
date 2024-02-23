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

        // Add Content-Type header if the contentType is not null
        if (contentType != null) {
            var a = (contentType.getValue());
            headers.add("Content-Type", a); // + "; charset=UTF-8"
        }

        // Add Content-Length header if the body is not null
        if (this.body != null) {
            // headers.add("Content-Length", Integer.toString(this.body.length()));
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
        // Create a StringBuilder to build the response
        StringBuilder responseBuilder = new StringBuilder();

        // Append the status line
        responseBuilder.append(this.getProtocol()).append(" ").append(getStatusCode()).append("\r\n");

        // Append headers
        for (String headerLine : this.getHeaders()) {
            responseBuilder.append(headerLine).append("\r\n");
        }

        // Add a blank line to separate headers from the body
        responseBuilder.append("\r\n");

        // Append the body
        if (this.body != null) {
            responseBuilder.append(this.body);
        }
        var result = responseBuilder.toString();
        out.write(result);

    }

    // public void writeTo(OutputStream out) {
    // // Create a PrintWriter from the OutputStream
    // PrintWriter writer = new PrintWriter(new OutputStreamWriter(out,
    // StandardCharsets.UTF_8));

    // // Write the status line
    // writer.print(this.getProtocol() + " " + getStatusCode() + "\r\n");

    // // Write headers
    // for (String headerLine : this.getHeaders()) {
    // writer.print(headerLine + "\r\n");
    // }

    // // Add a blank line to separate headers from the body
    // writer.print("\r\n");

    // // Write the body
    // if (this.body != null) {
    // writer.print(this.body);
    // }

    // // Flush the writer
    // writer.flush();
    // writer.close();
    // }

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
