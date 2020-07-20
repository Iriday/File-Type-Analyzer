package analyzer;

public class AnalyzerUtils {
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
}
