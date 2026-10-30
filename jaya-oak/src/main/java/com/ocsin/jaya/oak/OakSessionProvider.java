package com.ocsin.jaya.oak;

import com.ocsin.jaya.application.ContentAccessException;
import com.ocsin.jaya.jcr.SessionProvider;
import com.ocsin.jaya.jcr.SessionWork;
import org.apache.jackrabbit.api.JackrabbitRepository;
import org.apache.jackrabbit.oak.Oak;
import org.apache.jackrabbit.oak.jcr.Jcr;
import org.apache.jackrabbit.oak.segment.SegmentNodeStoreBuilders;
import org.apache.jackrabbit.oak.segment.file.FileStore;
import org.apache.jackrabbit.oak.segment.file.FileStoreBuilder;
import org.apache.jackrabbit.oak.segment.file.InvalidFileStoreVersionException;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Repository Oak local. Le mot de passe admin est celui du dépôt embarqué, pas celui de Jahia.
 */
public final class OakSessionProvider implements SessionProvider {

    private final Repository repository;
    private final FileStore fileStore;
    private final SimpleCredentials credentials = new SimpleCredentials("admin", "admin".toCharArray());

    private OakSessionProvider(Repository repository, FileStore fileStore) {
        this.repository = repository;
        this.fileStore = fileStore;
    }

    public static OakSessionProvider memory() {
        return new OakSessionProvider(new Jcr(new Oak()).createRepository(), null);
    }

    public static OakSessionProvider file(Path directory) {
        try {
            Files.createDirectories(directory);
            FileStore store = FileStoreBuilder.fileStoreBuilder(directory.toFile()).build();
            var nodeStore = SegmentNodeStoreBuilders.builder(store).build();
            Repository repository = new Jcr(new Oak(nodeStore)).createRepository();
            return new OakSessionProvider(repository, store);
        } catch (IOException | InvalidFileStoreVersionException ex) {
            throw new ContentAccessException("Ouverture du repository fichier impossible", ex);
        }
    }

    public static OakSessionProvider open(Path directory) {
        return directory == null ? memory() : file(directory);
    }

    @Override
    public <T> T inSession(SessionWork<T> work) {
        Session session = null;
        try {
            session = repository.login(credentials);
            return work.doInSession(session);
        } catch (RepositoryException ex) {
            throw new ContentAccessException("Accès au repository local impossible", ex);
        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }

    @Override
    public void close() {
        if (repository instanceof JackrabbitRepository jackrabbit) {
            jackrabbit.shutdown();
        }
        if (fileStore != null) {
            fileStore.close();
        }
    }
}
