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
        byte[] data;
        try {
            data = Files.readAllBytes(Path.of(args[0]));
        } catch (IOException e) {
            System.out.println("Something went wrong when trying to read file");
            return;
        }
        long start = System.currentTimeMillis();
        boolean result = AnalyzerUtils.contains(data, args[1].getBytes());
        long end = System.currentTimeMillis();

        System.out.println(result ? args[2] : "Unknown file type");
        System.out.println("It took " + ((end - start) / 1000.0) + " seconds");
    }
}
