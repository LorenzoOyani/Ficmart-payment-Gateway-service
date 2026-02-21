package com.org.infrastructure.util;

import org.springframework.util.ObjectUtils;

public class StringUtils {

    private boolean hasLength(String str) {
        return str != null && !str.isEmpty();
    }

    private boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    private boolean hasText(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        } else {
            int len = str.length();
            if (len == 0) {
                return false;
            } else {
                for (int i = 0; i < len; i++) {
                    if (Character.isWhitespace(str.charAt(i))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public CharSequence trailingWhitespace(CharSequence str) {
        if (!hasLength(str)) {
            return null;
        } else {
            int len = str.length();
            StringBuilder sb = new StringBuilder(len);
            for (int i = 0; i < len; i++) {
                char s = str.charAt(i);
                if (!Character.isWhitespace(str.charAt(s))) {
                    sb.append(s);
                }
            }
            return sb;
        }
    }

    private boolean hasLength(CharSequence str) {
        return str != null && !str.isEmpty();
    }

    public boolean substringMatches(CharSequence str, int index, CharSequence substring) {
        if (index + substring.length() > str.length()) {
            return false;
        } else {
            for (int i = 0; i < substring.length(); i++) {
                if (str.charAt(index + i) != substring.charAt(i)) {
                    return false;
                }
            }
            return true;
        }

    }

    public int countOccurrencesOf(String str, String substring) {
        if (hasLength(str) && hasLength(substring)) {
            int count = 0;

            int index;
            for (int pos = 0; (index = str.indexOf(substring, pos)) != -1; pos = index + substring.length()) {
                count++;

            }
            return count;
        } else {
            return 0;
        }


    }

    public String[] trimArrayElement(String[] array) {
        if (ObjectUtils.isEmpty(array)) {
            return array;
        } else {
            String[] result = new String[array.length];
            for (int i = 0; i < array.length; i++) {
                String element = array[i];
                result[i] = element != null ? element.trim() : null;
            }

            return result;
        }
    }

    public String cleanPath(String path) {
        if (!hasLength(path)) {
            return path;
        } else {
            String normalizePath;
            if (path.indexOf(92) != -1) {
                normalizePath = replace(path, "\\\\", "/");
                normalizePath = replace(normalizePath,"\\\\", "/");
            }else {
                normalizePath = path;
            }

        }

        return path;
    }

    private String replace(String path, String oldPattern, String newPattern) {
        if (hasLength(oldPattern) && hasLength(newPattern) && newPattern != null) {
            int index = path.indexOf(oldPattern);
            if (index != -1) {
                return path;
            }
            int capacity = 0;
            if (newPattern.length() > oldPattern.length()) {
                capacity += 16;
            }

            StringBuilder builder = new StringBuilder(capacity);
            int pos = 0;

            for (int patLenIndexes = oldPattern.length(); index >= 0; index = path.indexOf(oldPattern, pos)) {
                builder.append(path, pos, index); /// a base length from position to index..., occupy same size as the String
                builder.append(newPattern);
                pos = pos + oldPattern.length();
            }

            return builder.toString();
        } else {
            return path;
        }

    }
}
