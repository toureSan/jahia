package com.ocsin.jaya.runtime;

import com.ocsin.jaya.application.ContentAccessException;
import com.ocsin.jaya.domain.ArticleId;
import com.ocsin.jaya.domain.Principal;
import com.ocsin.jaya.domain.Role;
import com.ocsin.jaya.domain.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JahiaRuntimeTest {

    @Test
    @DisplayName("hors de Jahia, l'accès JCR explique qu'il faut le runtime Jahia")
    void explainsMissingJahia() {
        JayaRuntime runtime = JayaRuntime.jahia(JayaConfig.jahia(Map.of()));
        Principal editor = new Principal(new UserId("editor"), EnumSet.of(Role.EDITOR));
        try {
            assertThatThrownBy(() -> runtime.editorial().find(editor, ArticleId.generate()))
                    .isInstanceOf(ContentAccessException.class)
                    .hasMessageContaining("Jahia");
        } finally {
            runtime.close();
        }
    }
}
