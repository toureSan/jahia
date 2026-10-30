package com.ocsin.jaya.jcr;

/**
 * Applique la migration de modèle au premier accès, puis délègue.
 */
public final class MigratingSessionProvider implements SessionProvider {

    private final SessionProvider delegate;
    private final ModuleBootstrap bootstrap;
    private boolean ready;

    public MigratingSessionProvider(SessionProvider delegate, ModuleBootstrap bootstrap) {
        this.delegate = delegate;
        this.bootstrap = bootstrap;
    }

    @Override
    public <T> T inSession(SessionWork<T> work) {
        ensureReady();
        return delegate.inSession(work);
    }

    @Override
    public void close() {
        delegate.close();
    }

    private void ensureReady() {
        if (ready) {
            return;
        }
        synchronized (this) {
            if (ready) {
                return;
            }
            bootstrap.start(delegate);
            ready = true;
        }
    }
}
