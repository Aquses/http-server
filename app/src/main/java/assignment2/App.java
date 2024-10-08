/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package assignment2;

import java.nio.file.Files;
import java.nio.file.Path;

public class App {

    public static void main(String[] args) {
        int port = 80;
        String rootDir = "public";

        if (args.length != 2) {
        System.err.println("Usage: <portnumber> <public>");
        System.exit(1);
        }

        try {
        port = Integer.parseInt(args[0]);
        if (port < 0 || port >= 65535) {
        System.err.println("The port needs to be in the range of 0-65535");
        System.exit(1);
        }
        } catch (NumberFormatException ex) {
        System.err.println("The port must be an integer.");
        System.exit(1);
        return;
        }

        rootDir = args[1];
        
        Path path = Path.of(System.getProperty("user.dir"), rootDir);
        if (!Files.isDirectory(path)) {
            System.err.println("The directory \"" + path.toString() + "\" does not exist.");
            System.exit(1);
        }
        HttpServer server = new HttpServer(path.toString());
        try {
            server.start(port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
