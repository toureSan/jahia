package com.ocsin.jaya.application;

import com.ocsin.jaya.domain.Article;

import java.util.List;

public record SearchResult(List<Article> items, int total) {

    public SearchResult {
        items = List.copyOf(items);
        if (total < 0) {
            throw new IllegalArgumentException("Total négatif");
        }
    }

    public static SearchResult empty() {
        return new SearchResult(List.of(), 0);
    }
}
