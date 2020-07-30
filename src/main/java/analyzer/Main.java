package analyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static analyzer.AnalyzerUtils.searchAndMeasureTime;

public class Main {
    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
        if (args.length != 4) {
            System.out.println("Error, incorrect number of arguments. Expected: 4, found: " + args.length);
            return;
        }
        BiFunction<byte[], byte[], Boolean> searchAlg = AnalyzerUtils.getSearchAlgByName(args[0]);
        if (searchAlg == null) {
            System.out.println("Unknown search alg. Use: --naive or --KMP");
            return;
        }

        outputResults(startSearch(args[1], searchAlg, args[2].getBytes()), args[3]);
    }

    public static List<Path> getFilePaths(String pathToFileOrDirectory) throws IOException {
        Path path = Path.of(pathToFileOrDirectory);
        if (Files.isRegularFile(path)) {
            return List.of(path);
        }
        return Files.walk(path, 1)
                .filter(Files::isRegularFile)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static LinkedHashMap<String, Path> getFilenamesAndFilepaths(String pathToFileOrDirectory) throws IOException {
        var map = new LinkedHashMap<String, Path>();
        getFilePaths(pathToFileOrDirectory).forEach(v -> map.put(v.getFileName().toString(), v));
        return map;
    }

    public static LinkedHashMap<String, Future<long[]>> startSearch(String pathToFileOrDirectory, BiFunction<byte[], byte[], Boolean> searchAlg, byte[] pattern) throws IOException {
        var namesAndPaths = getFilenamesAndFilepaths(pathToFileOrDirectory);
        var namesAndResults = new LinkedHashMap<String, Future<long[]>>();

        ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        namesAndPaths.forEach((k, v) -> namesAndResults.put(k, service.submit(() -> searchAndMeasureTime(searchAlg, Files.readAllBytes(v), pattern))));
        service.shutdown();
        return namesAndResults;
    }

    public static void outputResults(Map<String, Future<long[]>> results, String pattern) throws ExecutionException, InterruptedException {
        long time = 0;
        for (Map.Entry<String, Future<long[]>> pair : results.entrySet()) {
            long[] result = pair.getValue().get();
            time += result[0];
            System.out.format("%s || %s || It took %s seconds\n", pair.getKey(), result[1] == 1 ? pattern : "Unknown file type", result[0] / 1000.0);
        }
        System.out.println("\nTotal: " + (time / 1000.0) + " seconds");
    }
}
