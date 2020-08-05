package analyzer;

public class SearchResult {
    final boolean found;
    final Pattern pattern;

    public SearchResult(boolean found, Pattern pattern) {
        this.found = found;
        this.pattern = pattern;
    }
}
