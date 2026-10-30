package com.ocsin.jaya.jcr;

import com.ocsin.jaya.application.ContentAccessException;
import org.apache.jackrabbit.commons.cnd.CndImporter;
import org.apache.jackrabbit.commons.cnd.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * Enregistre le CND et complète les articles déjà stockés lors d'une évolution de modèle.
 */
public final class ContentModelMigrator {

    private static final Logger log = LoggerFactory.getLogger(ContentModelMigrator.class);

    public void registerCurrent(Session session) {
        register(session, "META-INF/definitions.cnd");
    }

    public void register(Session session, String classpathResource) {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(classpathResource);
        if (in == null) {
            in = ContentModelMigrator.class.getClassLoader().getResourceAsStream(classpathResource);
        }
        if (in == null) {
            throw new ContentAccessException("CND introuvable : " + classpathResource);
        }
        try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            CndImporter.registerNodeTypes(reader, session, true);
            log.info("Modèle JCR enregistré depuis {}", classpathResource);
        } catch (IOException | RepositoryException | ParseException ex) {
            throw new ContentAccessException("Enregistrement du modèle JCR impossible", ex);
        }
    }

    public void backfill(Session session) throws RepositoryException {
        if (!session.getRootNode().hasNode("articles")) {
            return;
        }
        NodeIterator nodes = session.getRootNode().getNode("articles").getNodes();
        int updated = 0;
        while (nodes.hasNext()) {
            Node node = nodes.nextNode();
            if (!"jaya:article".equals(node.getPrimaryNodeType().getName())) {
                continue;
            }
            boolean changed = false;
            if (!node.hasProperty("jaya:titleLower") && node.hasProperty("jaya:title")) {
                node.setProperty("jaya:titleLower", TextNorm.searchable(node.getProperty("jaya:title").getString()));
                changed = true;
            }
            if (!node.hasProperty("jaya:bodyLower") && node.hasProperty("jaya:body")) {
                node.setProperty("jaya:bodyLower", TextNorm.searchable(node.getProperty("jaya:body").getString()));
                changed = true;
            }
            if (changed) {
                updated++;
            }
        }
        if (updated > 0) {
            log.info("Champs de recherche complétés sur {} article(s)", updated);
        }
    }

    public boolean hasProperty(Session session, String property) throws RepositoryException {
        NodeType type = session.getWorkspace().getNodeTypeManager().getNodeType("jaya:article");
        for (PropertyDefinition definition : type.getPropertyDefinitions()) {
            if (property.equals(definition.getName())) {
                return true;
            }
        }
        return false;
    }
}
