package assignment2;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HttpHeader {
    public Map<String, String> headers;

    public HttpHeader() {
        this.headers = new HashMap<>();
    }

    public HttpHeader(String[] header, int offset) throws Exception {
        headers = parse(header, offset);
    }

    public void addHeaderLine(String headerLine) {
        String[] parts = headerLine.split(": ", 2);
        if (parts.length == 2) {
            String key = parts[0];
            String value = parts[1];
            headers.put(key, value);
        } else {
            // Handle invalid header line
            System.err.println("Invalid header line: " + headerLine);
        }
    }

    public static Map<String, String> parse(String[] header, int offset) throws Exception {
        final Map<String, String> headers = new HashMap<>();

        for (int i = offset; i < header.length; i++) {
            String line = header[i].trim();

            if (line.isEmpty()) {
                return headers;
            }
            int colonIndex = line.indexOf(':');

            if (colonIndex != -1) {
                String key = line.substring(0, colonIndex).trim();
                String value = line.substring(colonIndex + 1).trim();
                headers.put(key, value);
            } else {
                throw new Exception("Invalid http header format: " + line);
            }
        }

        return headers;
    }

    public void add(String header, String value) {
        this.headers.put(header, value);
    }

    public void setStatusCode(StatusCode code) {
        headers.put("Status-Code", code.toString());
    }

    public Optional<StatusCode> getStatusCode() {
        // Use Optional.ofNullable to handle the case when the key is not present in the
        // map
        return Optional.ofNullable(headers.get("Status-Code")).map(StatusCode::valueOf);
    }

    public void setContentType(Mime mime) {
        headers.put("Content-Length", mime.toString());
    }

    public Optional<Mime> getContentType() {
        return Optional.ofNullable(headers.get("Content-Type"))
                .map(value -> {
                        return Mime.valueOfIgnoreCase(value);
                });

    }

    public String[] getHeaders() {
        return headers.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .toArray(String[]::new);
    }

    public int getLength() {
        return this.headers.size();
    }
}
