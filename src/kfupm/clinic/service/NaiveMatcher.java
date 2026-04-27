package kfupm.clinic.service;

public class NaiveMatcher implements StringMatcher {
    @Override
    public boolean contains(String text, String pattern) {
        if (text == null || pattern == null) return false;
        // Case-insensitive comparison
        String t = text.toLowerCase();
        String p = pattern.toLowerCase();
        int n = t.length(), m = p.length();
        if (m == 0) return true;
        if (m > n)  return false;

        for (int i = 0; i <= n - m; i++) {
            int j = 0;
            while (j < m && t.charAt(i + j) == p.charAt(j)) j++;
            if (j == m) return true;
        }
        return false;
    }
}