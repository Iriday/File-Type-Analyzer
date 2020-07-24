package analyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiFunction;

import static analyzer.AnalyzerUtils.searchAndMeasureTime;

public class Main {
    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Error, incorrect number of arguments. Expected: 4, found: " + args.length);
            return;
        }

        byte[] data;
        try {
            data = Files.readAllBytes(Path.of(args[1]));
        } catch (IOException e) {
            System.out.println("Something went wrong when trying to read file");
            return;
        }

        BiFunction<byte[], byte[], Boolean> searchAlg = AnalyzerUtils.getSearchAlgByName(args[0]);
        if (searchAlg == null) {
            System.out.println("Unknown search alg. Use: --naive or --KMP");
            return;
        }

        outputResult(searchAndMeasureTime(searchAlg, data, args[2].getBytes()), args[3]);
    }

    public static void outputResult(long[] result, String pattern) {
        System.out.println(result[1] == 1 ? pattern : "Unknown file type");
        System.out.println("It took " + (result[0] / 1000.0) + " seconds");
    }
}
