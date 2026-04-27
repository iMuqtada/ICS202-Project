    package kfupm.clinic.service;

    public class KMPMatcher implements StringMatcher {

        @Override
        public boolean contains(String text, String pattern) {
            if (text == null || pattern == null) return false;
            String t = text.toLowerCase();
            String p = pattern.toLowerCase();
            int n = t.length(), m = p.length();
            if (m == 0) return true;
            if (m > n)  return false;

            int[] lps = buildLPS(p);
            int i = 0, j = 0;
            while (i < n) {
                if (t.charAt(i) == p.charAt(j)) {
                    i++; j++;
                    if (j == m) return true;   // match found
                } else {
                    if (j != 0) j = lps[j - 1];
                    else        i++;
                }
            }
            return false;
        }

        /**
         * Builds the Longest Proper Prefix which is also Suffix (LPS) array.
         * lps[i] = length of the longest proper prefix of pattern[0..i]
         *          that is also a suffix of pattern[0..i].
         */
        private int[] buildLPS(String pattern) {
            int m = pattern.length();
            int[] lps = new int[m];
            lps[0] = 0;
            int len = 0, i = 1;
            while (i < m) {
                if (pattern.charAt(i) == pattern.charAt(len)) {
                    lps[i++] = ++len;
                } else {
                    if (len != 0) len = lps[len - 1];
                    else          lps[i++] = 0;
                }
            }
            return lps;
        }
    }