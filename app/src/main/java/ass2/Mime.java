package ass2;

import java.util.HashMap;
import java.util.Map;

public enum Mime {
    PNG("image/png"),
    JPEG("image/jpeg"),
    GIF("image/gif"),
    APPLICATION_OCTET_STREAM("application/octet-stream"),
    HTML("text/html"),
    TXT("text/plain");

    private final String value;

    Mime(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    // Method to get MIME type by file extension
    public static Mime getByFileExtension(String extension) {
        // Add more file extensions and their corresponding Mime types as needed
        Map<String, Mime> fileExtensionToMimeMap = new HashMap<>();
        fileExtensionToMimeMap.put("png", Mime.PNG);
        fileExtensionToMimeMap.put("jpeg", Mime.JPEG);
        fileExtensionToMimeMap.put("jpg", Mime.JPEG);
        fileExtensionToMimeMap.put("gif", Mime.GIF);
        fileExtensionToMimeMap.put("txt", Mime.TXT);
        fileExtensionToMimeMap.put("html", Mime.HTML);

        // Default to octet-stream if the extension is not found
        return fileExtensionToMimeMap.getOrDefault(extension.toLowerCase(), Mime.APPLICATION_OCTET_STREAM);
    }

}
