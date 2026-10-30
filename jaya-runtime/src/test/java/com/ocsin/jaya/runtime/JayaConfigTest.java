package com.ocsin.jaya.runtime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JayaConfigTest {

    @Test
    @DisplayName("la production refuse de démarrer sans jetons explicites")
    void productionRequiresTokens() {
        assertThatThrownBy(() -> JayaConfig.from(Map.of("JAYA_ENV", "production")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JAYA_TOKEN_READER");
    }

    @Test
    void rejectsNonHttpExternalSystem() {
        assertThatThrownBy(() -> JayaConfig.from(Map.of("JAYA_EXTERNAL_URL", "file:///tmp/hook")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("http");
    }

    @Test
    void localGeneratesTokens() {
        JayaConfig config = JayaConfig.from(Map.of("JAYA_DATA_DIR", "memory"));
        assertThat(config.devTokens()).isTrue();
        assertThat(config.dataDir()).isNull();
        assertThat(config.tokens().reader()).hasSize(64);
        assertThat(config.host()).isEqualTo("127.0.0.1");
    }
}
