package com.ocsin.jaya.jcr;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

public interface SessionWork<T> {

    T doInSession(Session session) throws RepositoryException;
}
