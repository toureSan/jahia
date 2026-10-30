package com.ocsin.jaya.app;

import com.ocsin.jaya.application.port.ExternalPublisher;
import com.ocsin.jaya.oak.OakSessionProvider;
import com.ocsin.jaya.runtime.JayaConfig;
import com.ocsin.jaya.runtime.JayaRuntime;
import com.ocsin.jaya.security.TokenDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Point d'entrée du serveur local. Le module OSGi n'utilise pas cette classe.
 */
public final class JayaApplication {

    private static final Logger log = LoggerFactory.getLogger(JayaApplication.class);

    private JayaApplication() {
    }

    public static void main(String[] args) {
        JayaConfig config = JayaConfig.fromEnvironment();
        JayaHandle handle = boot(config, null);
        Runtime.getRuntime().addShutdownHook(new Thread(handle::close, "jaya-shutdown"));
        log.info("Jaya écoute sur http://{}:{}", config.host(), config.port());
        if (config.devTokens()) {
            TokenDirectory tokens = config.tokens();
            log.info("Jetons locaux (démonstration uniquement) reader={} editor={} publisher={} admin={}",
                    tokens.reader(), tokens.editor(), tokens.publisher(), tokens.admin());
        }
    }

    public static JayaHandle boot(JayaConfig config, ExternalPublisher publisher) {
        OakSessionProvider sessions = OakSessionProvider.open(config.dataDir());
        JayaRuntime runtime = JayaRuntime.standalone(sessions, config, publisher);
        try {
            runtime.warmup();
            if (config.seed()) {
                new ContentSeeder().seedIfEmpty(runtime.editorial());
            }
            return new JayaHandle(JayaHttpServer.start(runtime, config), runtime);
        } catch (RuntimeException ex) {
            runtime.close();
            throw ex;
        }
    }
}
