package analyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Error, incorrect number of arguments. Expected: 3, found: " + args.length);
            return;
        }
        try {
            boolean result = AnalyzerUtils.contains(Files.readAllBytes(Path.of(args[0])), args[1].getBytes());
            System.out.println(result ? args[2] : "Unknown file type");
        } catch (IOException e) {
            System.out.println("Something went wrong when trying to read file");
        }
    }
}
