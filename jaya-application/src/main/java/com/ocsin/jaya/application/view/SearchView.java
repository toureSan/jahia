package com.ocsin.jaya.application.view;

import com.ocsin.jaya.application.SearchResult;
import com.ocsin.jaya.domain.Article;

import java.util.ArrayList;
import java.util.List;

public final class SearchView {

    private final int total;
    private final List<ArticleView> items;

    private SearchView(SearchResult result) {
        this.total = result.total();
        List<ArticleView> views = new ArrayList<>();
        for (Article article : result.items()) {
            views.add(ArticleView.of(article));
        }
        this.items = List.copyOf(views);
    }

    public static SearchView of(SearchResult result) {
        return new SearchView(result);
    }

    public int getTotal() {
        return total;
    }

    public List<ArticleView> getItems() {
        return items;
    }
}
