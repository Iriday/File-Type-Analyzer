package analyzer;

import java.util.function.BiFunction;

public class AnalyzerUtils {
    public static BiFunction<byte[], byte[], Boolean> getSearchAlgByName(String name) {
        if (name.equals("--naive")) return AnalyzerUtils::contains;
        if (name.equals("--KMP")) return AnalyzerUtils::KMPContains;
        return null;
    }

    public static SearchResult searchAndMeasureTime(BiFunction<byte[], byte[], Boolean> searchAlg, byte[] data, Pattern pattern) {
        long start = System.currentTimeMillis();
        boolean found = searchAlg.apply(data, pattern.pattern);
        long end = System.currentTimeMillis();

        return new SearchResult(end - start, found, pattern);
    }

    public static boolean contains(byte[] data, byte[] pattern) {
        for (int i = 0; i <= data.length - pattern.length; i++) {
            for (int j = 0; j < pattern.length; j++) {
                if (data[j + i] != pattern[j]) {
                    break;
                }
                if (j == pattern.length - 1) {
                    return true;
                }
            }
        }
        return false;
    }

    // Knuth Morris Pratt alg
    public static boolean KMPContains(byte[] str, byte[] pattern) {
        int[] prefFun = prefixFunction(pattern);
        int j = 0;
        for (int i = 0; i < str.length; i++) {
            while (j > 0 && str[i] != pattern[j]) {
                j = prefFun[j - 1];
            }
            if (str[i] == pattern[j]) {
                j++;
            }
            if (j == pattern.length) {
                return true;
            }
        }
        return false;
    }

    public static int[] prefixFunction(byte[] s) {
        int[] prefFun = new int[s.length];

        for (int i = 1; i < s.length; i++) {
            int j = prefFun[i - 1];

            while (j > 0 && s[i] != s[j]) {
                j = prefFun[j - 1];
            }
            if (s[i] == s[j]) {
                j++;
            }
            prefFun[i] = j;
        }
        return prefFun;
    }
}
