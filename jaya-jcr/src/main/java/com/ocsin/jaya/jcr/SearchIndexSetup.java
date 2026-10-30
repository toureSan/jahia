package com.ocsin.jaya.jcr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Index de propriétés Oak pour les filtres de statut, de locale et de tri.
 * Sans nœud oak:index (repository non Oak), l'installation est ignorée.
 */
public final class SearchIndexSetup {

    private static final Logger log = LoggerFactory.getLogger(SearchIndexSetup.class);

    public void ensure(Session session) throws RepositoryException {
        if (!session.getRootNode().hasNode("oak:index")) {
            log.info("Pas de nœud oak:index, indexation Oak ignorée");
            return;
        }
        Node indexes = session.getRootNode().getNode("oak:index");
        ensurePropertyIndex(indexes, "jayaStatus", "jaya:status");
        ensurePropertyIndex(indexes, "jayaLocale", "jaya:locale");
        ensurePropertyIndex(indexes, "jayaUpdatedAt", "jaya:updatedAt");
        ensurePropertyIndex(indexes, "jayaId", "jaya:id");
        ensurePropertyIndex(indexes, "jayaTitleLower", "jaya:titleLower");
    }

    private static void ensurePropertyIndex(Node indexes, String name, String property) throws RepositoryException {
        if (indexes.hasNode(name)) {
            return;
        }
        Node index = indexes.addNode(name, "oak:QueryIndexDefinition");
        index.setProperty("type", "property");
        index.setProperty("propertyNames", new String[]{property}, PropertyType.NAME);
        index.setProperty("reindex", true);
        index.setProperty("unique", false);
    }
}
