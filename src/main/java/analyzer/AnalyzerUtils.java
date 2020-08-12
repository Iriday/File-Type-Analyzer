package analyzer;

import java.util.function.BiFunction;

public class AnalyzerUtils {
    public static BiFunction<byte[], byte[], Boolean> getSearchAlgByName(String name) {
        if (name.equals("--naive")) return AnalyzerUtils::contains;
        if (name.equals("--KMP")) return AnalyzerUtils::KMPContains;
        return null;
    }

    public static SearchResult startSearch(BiFunction<byte[], byte[], Boolean> searchAlg, byte[] data, Pattern pattern) {
        boolean found = searchAlg.apply(data, pattern.pattern);
        return new SearchResult(found, pattern);
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

    // Rabin Karp alg
    public static boolean RKContains(String str, String pattern) {
        if (str.length() < pattern.length()) {
            return false;
        }

        int a = 53;
        long m = 1_000_000_009;

        long patternHash = 0;
        long currStrHash = 0;
        long pow = 1;

        for (int i = 0; i < pattern.length(); i++) {
            patternHash += hash(pattern.charAt(i)) * pow;
            patternHash %= m;
            currStrHash += hash(str.charAt(str.length() - pattern.length() + i)) * pow;
            currStrHash %= m;

            if (i != pattern.length() - 1) {
                pow = pow * a % m;
            }
        }

        for (int i = str.length(); i >= pattern.length(); i--) {
            if (patternHash == currStrHash) {
                for (int j = 0; j < pattern.length() && pattern.charAt(j) == str.charAt(i - pattern.length() + j); j++) {
                    if (j == pattern.length() - 1) {
                        return true;
                    }
                }
            }
            if (i > pattern.length()) {
                currStrHash = (currStrHash - hash(str.charAt(i - 1)) * pow % m + m) * a % m;
                currStrHash = (currStrHash + hash(str.charAt(i - pattern.length() - 1))) % m;
            }
        }
        return false;
    }

    public static int hash(char c) {
        return c - 'A' + 1;
    }
}
