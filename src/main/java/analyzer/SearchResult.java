package analyzer;

public class SearchResult {
    final long time;
    final boolean found;
    final Pattern pattern;

    public SearchResult(long time, boolean found, Pattern pattern) {
        this.time = time;
        this.found = found;
        this.pattern = pattern;
    }
}
