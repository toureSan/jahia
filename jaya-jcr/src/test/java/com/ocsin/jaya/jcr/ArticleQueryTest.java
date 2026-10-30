package com.ocsin.jaya.jcr;

import com.ocsin.jaya.application.SearchCriteria;
import com.ocsin.jaya.domain.Visibility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleQueryTest {

    @Test
    @DisplayName("une tentative d'injection SQL2 reste un paramètre lié")
    void bindsUserTerm() {
        String payload = "%' OR '1'='1";
        ArticleQuery query = ArticleQuery.build(new SearchCriteria(payload, null, "fr", 10, 0), Visibility.PUBLISHED_ONLY);

        assertThat(query.select()).doesNotContain(payload);
        assertThat(query.select()).contains("LIKE $term");
        assertThat(query.bindings()).containsEntry("status", "PUBLISHED");
        assertThat(query.bindings()).containsEntry("term", "%\\%' or '1'='1%");
    }
}
