package analyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
        run(args);
    }

    public static void run(String[] args) throws IOException, ExecutionException, InterruptedException {
        if (args.length != 4) {
            System.out.println("Error, incorrect number of arguments. Expected: 4, found: " + args.length);
            return;
        }
        String searchAlgName = args[0];
        String pathToFileOrDirectory = args[1];
        String pattern = args[2];
        String expectedType = args[3];

        BiFunction<byte[], byte[], Boolean> searchAlg = AnalyzerUtils.getSearchAlgByName(searchAlgName);
        if (searchAlg == null) {
            System.out.println("Unknown search alg. Use: --naive or --KMP");
            return;
        }
        var namesAndPaths = pathsToNamesAndPaths(getFilePaths(pathToFileOrDirectory));

        outputResults(startSearch(namesAndPaths, searchAlg, pattern.getBytes()), expectedType);
    }

    public static List<Path> getFilePaths(String pathToFileOrDirectory) throws IOException {
        Path path = Path.of(pathToFileOrDirectory);
        if (Files.isRegularFile(path)) {
            return List.of(path);
        }
        return Files.walk(path, 1)
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());
    }

    public static Map<String, Path> pathsToNamesAndPaths(List<Path> paths) {
        return paths.stream().collect(Collectors.toMap(path -> path.getFileName().toString(), path -> path, (a, b) -> a, LinkedHashMap::new));
    }

    public static LinkedHashMap<String, Future<long[]>> startSearch(Map<String, Path> namesAndPaths, BiFunction<byte[], byte[], Boolean> searchAlg, byte[] pattern) {
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
