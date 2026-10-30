package com.ocsin.jaya.application.port;

import com.ocsin.jaya.application.SearchCriteria;
import com.ocsin.jaya.application.SearchResult;
import com.ocsin.jaya.domain.Visibility;

public interface ArticleSearch {

    SearchResult search(SearchCriteria criteria, Visibility visibility);
}
