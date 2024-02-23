package ass2;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HttpHeader {
    private Map<String, String> headers;

    public HttpHeader() {
        this.headers = new HashMap<>();
    }

    public HttpHeader(String[] header, int offset) throws Exception {
        headers = parse(header, offset);
    }

    public static Map<String, String> parse(String[] header, int offset) throws Exception {
        final Map<String, String> headers = new HashMap<>();

        for (int i = offset; i < header.length; i++) {
            String line = header[i].trim(); // Trim to remove leading/trailing whitespaces

            if (line.isEmpty()) { // End of HTML header.
                return headers;
            }

            // Check if the line contains a colon
            int colonIndex = line.indexOf(':');

            if (colonIndex != -1) {
                String key = line.substring(0, colonIndex).trim();
                String value = line.substring(colonIndex + 1).trim();
                headers.put(key, value);
            } else {
                // Handle the case where the line doesn't contain a colon
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
        return Optional.ofNullable(headers.get("Content-Type")).map(Mime::valueOf);
    }

    public String[] getHeaders() {
        return headers.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .toArray(String[]::new);
    }
}
