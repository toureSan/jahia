package com.ocsin.jaya.osgi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.assertj.core.api.Assertions.assertThat;

class BundleContentIT {

    @Test
    @DisplayName("le bundle Jahia embarque le modèle, le service et le cache, pas un second Oak")
    void shadedModuleContainsTheRuntime() throws Exception {
        Path jar = Path.of("target", "jaya-module.jar");
        assertThat(jar).exists();
        try (JarFile archive = new JarFile(jar.toFile())) {
            assertThat(read(archive, "META-INF/definitions.cnd")).contains("[jaya:article]");
            assertThat(archive.getEntry("OSGI-INF/com.ocsin.jaya.osgi.JayaModule.xml")).isNotNull();
            assertThat(archive.getEntry("com/ocsin/jaya/osgi/JayaModule.class")).isNotNull();
            assertThat(archive.getEntry("com/ocsin/jaya/jcr/JcrArticleStore.class")).isNotNull();
            assertThat(archive.getEntry("com/github/benmanes/caffeine/cache/Caffeine.class")).isNotNull();
            assertThat(archive.getEntry("org/apache/jackrabbit/oak/Oak.class")).isNull();
            assertThat(archive.getManifest().getMainAttributes().getValue("Service-Component"))
                    .contains("JayaModule");
            assertThat(archive.getManifest().getMainAttributes().getValue("Jahia-Module-Type"))
                    .isEqualTo("module");
        }
    }

    private static String read(JarFile archive, String name) throws Exception {
        JarEntry entry = archive.getJarEntry(name);
        assertThat(entry).isNotNull();
        return new String(archive.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8);
    }
}
