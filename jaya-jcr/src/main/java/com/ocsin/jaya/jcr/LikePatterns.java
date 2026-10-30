package com.ocsin.jaya.jcr;

import java.util.Locale;

/**
 * Échappe les jokers LIKE pour qu'une saisie utilisateur reste littérale.
 */
public final class LikePatterns {

    private LikePatterns() {
    }

    /**
     * Entoure le terme de {@code %} et échappe {@code %}, {@code _} et {@code \}.
     * Oak applique cet échappement par défaut et refuse la clause SQL2 {@code ESCAPE}.
     */
    public static String contains(String term) {
        String lower = term.toLowerCase(Locale.ROOT);
        StringBuilder escaped = new StringBuilder(lower.length() + 2);
        escaped.append('%');
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            if (c == '%' || c == '_' || c == '\\') {
                escaped.append('\\');
            }
            escaped.append(c);
        }
        escaped.append('%');
        return escaped.toString();
    }
}
