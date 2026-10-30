package com.ocsin.jaya.jcr;

/**
 * Prépare le repository : types de nœuds, rattrapage des contenus, index de recherche.
 */
public final class ModuleBootstrap {

    private final ContentModelMigrator migrator;
    private final SearchIndexSetup indexes;
    private final boolean registerTypes;
    private final boolean installIndexes;

    private ModuleBootstrap(boolean registerTypes, boolean installIndexes) {
        this.migrator = new ContentModelMigrator();
        this.indexes = new SearchIndexSetup();
        this.registerTypes = registerTypes;
        this.installIndexes = installIndexes;
    }

    public static ModuleBootstrap standalone() {
        return new ModuleBootstrap(true, true);
    }

    public static ModuleBootstrap jahia() {
        return new ModuleBootstrap(false, false);
    }

    public void start(SessionProvider sessions) {
        sessions.inSession(session -> {
            if (registerTypes) {
                migrator.registerCurrent(session);
            }
            migrator.backfill(session);
            if (installIndexes) {
                indexes.ensure(session);
            }
            session.save();
            return null;
        });
    }
}
