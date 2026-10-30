package com.ocsin.jaya.app;

import com.ocsin.jaya.rest.ProblemBody;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Refuse un corps au-delà de 64 Ko, y compris sans en-tête Content-Length.
 */
final class BodyLimit implements ContainerRequestFilter, ReaderInterceptor {

    static final int MAX_BYTES = 64 * 1024;

    @Override
    public void filter(ContainerRequestContext request) {
        if (request.getLength() > MAX_BYTES) {
            request.abortWith(rejected());
            return;
        }
        request.setEntityStream(new BoundedInputStream(request.getEntityStream(), request.getLength(), MAX_BYTES));
    }

    @Override
    public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
        try {
            return context.proceed();
        } catch (IOException ex) {
            if (causedByLimit(ex)) {
                throw new WebApplicationException(rejected());
            }
            throw ex;
        }
    }

    private static Response rejected() {
        return Response.status(413)
                .type(MediaType.valueOf("application/problem+json"))
                .entity(ProblemBody.payloadTooLarge())
                .build();
    }

    private static boolean causedByLimit(Throwable error) {
        Throwable current = error;
        for (int depth = 0; current != null && depth < 8; depth++) {
            if (current instanceof TooLargeException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static final class TooLargeException extends IOException {
    }

    private static final class BoundedInputStream extends FilterInputStream {

        private final long contentLength;
        private final int max;
        private long seen;

        private BoundedInputStream(InputStream in, long contentLength, int max) {
            super(in);
            this.contentLength = contentLength;
            this.max = max;
        }

        @Override
        public int read() throws IOException {
            byte[] one = new byte[1];
            int count = read(one, 0, 1);
            return count < 0 ? -1 : one[0] & 0xff;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            int allowed = allowed(length);
            if (allowed <= 0) {
                return -1;
            }
            int count = in.read(buffer, offset, allowed);
            if (count > 0) {
                seen += count;
                if (seen > max) {
                    throw new TooLargeException();
                }
            }
            return count;
        }

        private int allowed(int length) {
            long remaining = max - seen;
            if (contentLength >= 0) {
                remaining = Math.min(remaining, contentLength - seen);
            }
            if (remaining <= 0) {
                return 0;
            }
            return (int) Math.min(length, remaining);
        }
    }
}
