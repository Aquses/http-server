package ass2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private RequestMethod requestMethod;
    private HttpHeader header;
    private String body;
    private URI uri;
    private String protocol;
    private Map<String, String> headers;
    private Map<String, String> parameters;
    private Map<String, String> credentials;

    public HttpRequest(BufferedReader requestStream) throws Exception {
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = requestStream.readLine()) != null && !line.isEmpty()) {
            sb.append(line).append("\n");
        }
        sb.deleteCharAt(sb.length() - 1);

        String rawRequest = sb.toString();
        System.out.println("Raw Request:\n" + rawRequest);
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
        this.credentials = loadCredentials();
        this.parameters = new HashMap<>();

        if (RequestMethod.POST == this.requestMethod) {
            String postData = lines[lines.length - 1];
            System.out.println("\nPOST Data:\n" + postData);  // Log POST data
        
            // Add this line to debug
            System.out.println("\nRaw POST Data:\n" + rawRequest);
        
            parsePostData(postData);
        }
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

    public Map<String, String> getParameters() {
        return parameters;
    }

    public Map<String, String> getCredentials() {
        return credentials;
    }

    // ||||| LOGIN ||||||
    private Map<String, String> loadCredentials() throws FileNotFoundException, IOException {
        Map<String, String> credentials = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("credentials.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    credentials.put(key, value);
                }
            }
        }
        return credentials;
    }

    // ||||| POST ||||||
    private void parsePostData(String postData) throws UnsupportedEncodingException {
        System.out.println("Raw POST Data: " + postData);  // Add this line for debugging
        
        if (postData != null && !postData.isEmpty()) {
            String[] pairs = postData.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8.toString());
                    String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8.toString());
    
                    // Debugging output
                    System.out.println("Key: " + key + ", Value: " + value);
    
                    parameters.put(key, value);
                }
            }
        }
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
