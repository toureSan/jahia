package com.ocsin.jaya.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArticleIdTest {

    @Test
    @DisplayName("un identifiant qui ressemble à un chemin JCR est refusé")
    void rejectsPathTraversal() {
        assertThatThrownBy(() -> ArticleId.parse("../articles"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ArticleId.parse("articles/secret"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void normalizesCase() {
        ArticleId id = ArticleId.parse("AAAAAAAA-BBBB-CCCC-DDDD-EEEEEEEEEEEE");
        assertThat(id.value()).isEqualTo("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    }
}
