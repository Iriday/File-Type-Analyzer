package analyzer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class Pattern {
    public final int priority;
    public final byte[] pattern;
    public final String name;

    public Pattern(int priority, byte[] pattern, String name) {
        this.priority = priority;
        this.pattern = pattern;
        this.name = name;
    }

    public static List<Pattern> readPatternsFromFile(String filepath) throws IOException {
        return Files.readAllLines(Path.of(filepath)).stream().map(line -> {
            int first = line.indexOf(";");
            int second = line.lastIndexOf(";");
            return new Pattern(Integer.parseInt(line.substring(0, first)), line.substring(first + 2, second - 1).getBytes(), line.substring(second + 2, line.length() - 1));
        }).collect(Collectors.toList());
    }
}
