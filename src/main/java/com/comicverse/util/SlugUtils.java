package com.comicverse.util; // ✅ Đã đổi thành util

import java.text.Normalizer;
import java.util.regex.Pattern;

public class SlugUtils {
    public static String toSlug(String input) {
        if (input == null) return "";
        
        String str = input.toLowerCase();
        
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        str = pattern.matcher(nfdNormalizedString).replaceAll("");
        
        str = str.replace("đ", "d");
        str = str.replaceAll("[^a-z0-9\\s-]", "");
        str = str.replaceAll("\\s+", "-");
        
        return str;
    }
}