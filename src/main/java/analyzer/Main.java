package analyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static analyzer.AnalyzerUtils.searchAndMeasureTime;

public class Main {
    private static final String defaultPatternsPath = "src/main/java/analyzer/patterns/patterns.db";
    private static final String defaultSearchAlg = "--KMP";

    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {
        run(args);
    }

    public static void run(String[] args) throws IOException, ExecutionException, InterruptedException {
        if (!(args.length >= 1 && args.length <= 3)) {
            System.out.println("Error, incorrect number of arguments");
            return;
        }
        if (args.length == 1) {
            args = new String[]{defaultSearchAlg, args[0], defaultPatternsPath};
        } else if (args.length == 2) {
            if (args[0].startsWith("--")) {
                args = new String[]{args[0], args[1], defaultPatternsPath};
            } else {
                args = new String[]{defaultSearchAlg, args[0], args[1]};
            }
        }

        String searchAlgName = args[0];
        String pathToFileOrDirectory = args[1];
        String pathToPatterns = args[2];
        List<Pattern> patterns = Pattern.readPatternsFromFile(pathToPatterns);

        BiFunction<byte[], byte[], Boolean> searchAlg = AnalyzerUtils.getSearchAlgByName(searchAlgName);
        if (searchAlg == null) {
            System.out.println("Unknown search alg. Use: --naive or --KMP");
            return;
        }
        var namesAndPaths = pathsToNamesAndPaths(getFilePaths(pathToFileOrDirectory));
        var namesAndData = readFiles(namesAndPaths);

        var results = startSearch(namesAndData, searchAlg, patterns);
        var resultsReduced = reduceResults(results);

        outputResults(resultsReduced);
    }

    private static LinkedHashMap<String, SearchResult> reduceResults(Map<String, List<Future<SearchResult>>> results) throws ExecutionException, InterruptedException {
        var rs = new LinkedHashMap<String, SearchResult>();
        for (Map.Entry<String, List<Future<SearchResult>>> pair : results.entrySet()) {
            var value = new ArrayList<SearchResult>();
            for (Future<SearchResult> f : pair.getValue()) {
                value.add(f.get());
            }
            long time = value.stream().mapToLong(v -> v.time).sum();
            Pattern pattern = value.stream().filter(v -> v.found).map(sr -> sr.pattern).max(Comparator.comparingInt(p -> p.priority)).orElse(null);

            rs.put(pair.getKey(), new SearchResult(time, pattern != null, pattern));
        }
        return rs;
    }

    private static LinkedHashMap<String, byte[]> readFiles(Map<String, Path> namesAndPaths) throws IOException {
        var namesAndData = new LinkedHashMap<String, byte[]>();
        for (Map.Entry<String, Path> pair : namesAndPaths.entrySet()) {
            namesAndData.put(pair.getKey(), Files.readAllBytes(pair.getValue()));
        }
        return namesAndData;
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

    public static LinkedHashMap<String, Path> pathsToNamesAndPaths(List<Path> paths) {
        return paths.stream().collect(Collectors.toMap(path -> path.getFileName().toString(), path -> path, (a, b) -> a, LinkedHashMap::new));
    }

    public static LinkedHashMap<String, List<Future<SearchResult>>> startSearch(Map<String, byte[]> namesAndPaths, BiFunction<byte[], byte[], Boolean> searchAlg, List<Pattern> patterns) {
        var namesAndResults = new LinkedHashMap<String, List<Future<SearchResult>>>();

        ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        namesAndPaths.forEach((kay, value) -> namesAndResults.put(kay, patterns.stream()
                .map(pattern ->
                        service.submit(() ->
                                searchAndMeasureTime(searchAlg, value, pattern)))
                .collect(Collectors.toList())));
        service.shutdown();
        return namesAndResults;
    }

    public static void outputResults(Map<String, SearchResult> results) {
        long time = 0;
        for (Map.Entry<String, SearchResult> pair : results.entrySet()) {
            SearchResult rs = pair.getValue();
            time += rs.time;
            System.out.format("%s || %s || It took %s seconds\n", pair.getKey(), rs.found ? rs.pattern.name : "Unknown file type", rs.time / 1000.0);
        }
        System.out.println("\nTotal: " + (time / 1000.0) + " seconds");
    }
}
