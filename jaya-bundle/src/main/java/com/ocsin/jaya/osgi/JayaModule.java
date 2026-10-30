package com.ocsin.jaya.osgi;

import com.ocsin.jaya.application.DiagnosticsService;
import com.ocsin.jaya.application.EditorialService;
import com.ocsin.jaya.runtime.JayaConfig;
import com.ocsin.jaya.runtime.JayaRuntime;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Démarre le module dans Jahia sans ouvrir de session au moment de l'activation.
 * Le CND de META-INF/definitions.cnd est pris en charge par Jahia ; le rattrapage se fait au premier appel.
 */
public final class JayaModule {

    private static final Logger log = LoggerFactory.getLogger(JayaModule.class);

    private JayaRuntime runtime;
    private ServiceRegistration<EditorialService> editorial;
    private ServiceRegistration<DiagnosticsService> diagnostics;

    public void activate(BundleContext context) {
        runtime = JayaRuntime.jahia(JayaConfig.jahia(System.getenv()));
        editorial = context.registerService(EditorialService.class, runtime.editorial(), null);
        diagnostics = context.registerService(DiagnosticsService.class, runtime.diagnostics(), null);
        log.info("Module Jaya actif");
    }

    public void deactivate() {
        unregister(editorial);
        unregister(diagnostics);
        if (runtime != null) {
            runtime.close();
        }
        log.info("Module Jaya arrêté");
    }

    private static void unregister(ServiceRegistration<?> registration) {
        if (registration == null) {
            return;
        }
        try {
            registration.unregister();
        } catch (IllegalStateException ex) {
            log.debug("Service OSGi déjà retiré");
        }
    }
}
