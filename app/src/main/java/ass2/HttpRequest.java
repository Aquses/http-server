package ass2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpRequest {
    private RequestMethod requestMethod;
    private HttpHeader header;
    private String body;
    private URI uri;
    private String protocol;
    private Map<String, String> headers;

    public HttpRequest(BufferedReader requestStream) throws Exception {
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = requestStream.readLine()) != null && !line.isEmpty()) {
            sb.append(line).append("\n");
        }

        // Remove trailing newline
        sb.deleteCharAt(sb.length() - 1);

        String rawRequest = sb.toString();
        String[] lines = rawRequest.split("\\r?\\n");

        if (lines.length < 3) {
            throw new Exception("Invalid HTTP request format");
        }

        String[] requestLine = lines[0].split("\\s");

        if (requestLine.length < 3) {
            throw new Exception("Invalid HTTP request line format");
        }

        this.requestMethod = RequestMethod.valueOf(requestLine[0]);
        this.uri = URI.create(requestLine[1]);
        this.protocol = requestLine[2];

        this.header = new HttpHeader(lines, 1);

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

    public String[] getHeaders() {
        return headers.keySet().toArray(new String[0]);
    }

    public void writeTo(OutputStreamWriter out) throws IOException {
        out.append(this.getRequestMethod().toString()).append(" ")
                .append(this.getUri().toString()).append(" ")
                .append(this.getProtocol().toString()).append("\r\n");

        for (String headerLine : this.getHeaders()) {
            out.append(headerLine).append("\r\n");
        }

        // Add a blank line to separate headers from the body
        out.append("\r\n");
        out.append(this.body);
    }
}
