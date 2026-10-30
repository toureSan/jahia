package com.ocsin.jaya.application;

import com.ocsin.jaya.domain.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InputGuardTest {

    private final InputGuard guard = new InputGuard();

    @Test
    void normalizesTagsAndLocale() {
        SaveArticle safe = guard.sanitize(new SaveArticle(null, "  Titre  ", " ", "Corps", null, List.of("Jahia", "jahia")));
        assertThat(safe.title()).isEqualTo("Titre");
        assertThat(safe.summary()).isNull();
        assertThat(safe.locale()).isEqualTo("fr");
        assertThat(safe.tags()).containsExactly("jahia");
    }

    @Test
    void rejectsControlCharactersAndOversizedLimits() {
        assertThatThrownBy(() -> guard.sanitize(new SaveArticle(null, "A\u0000B", null, "Corps", "fr", List.of())))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> guard.sanitize(new SearchCriteria("ok", null, "fr", 500, 0)))
                .isInstanceOf(ValidationException.class);
    }
}
