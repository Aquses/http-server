package assignment2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Credentials {
    public String username;
    public String password;

    public Credentials(String username, String password) {
        this.password = password;
        this.username = username;
    }

    public static Credentials loadCredentials() throws FileNotFoundException, IOException {
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
        return new Credentials(credentials.get("username"), credentials.get("password"));
    }
}