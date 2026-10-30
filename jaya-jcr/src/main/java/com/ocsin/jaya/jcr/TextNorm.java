package com.ocsin.jaya.jcr;

import java.util.Locale;

public final class TextNorm {

    private TextNorm() {
    }

    public static String searchable(String value) {
        return value.toLowerCase(Locale.ROOT).trim();
    }
}
