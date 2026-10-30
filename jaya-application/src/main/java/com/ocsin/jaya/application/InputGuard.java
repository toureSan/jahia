package com.ocsin.jaya.application;

import com.ocsin.jaya.domain.ValidationException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Valide les entrées avant qu'elles n'atteignent JCR ou une requête.
 */
public final class InputGuard {

    private static final Pattern LOCALE = Pattern.compile("^[a-z]{2}(?:-[A-Z]{2})?$");
    private static final Pattern TAG = Pattern.compile("^[a-z0-9](?:[a-z0-9-]{0,31})$");

    public SaveArticle sanitize(SaveArticle command) {
        String title = singleLine(command.title(), "Le titre", 180, true);
        String summary = singleLine(command.summary(), "Le résumé", 280, false);
        String body = multiline(command.body(), "Le corps", 20_000, true);
        String locale = command.locale() == null || command.locale().isBlank() ? "fr" : command.locale().trim();
        if (!LOCALE.matcher(locale).matches()) {
            throw new ValidationException("La locale est invalide");
        }
        return new SaveArticle(command.id(), title, summary, body, locale, tags(command.tags()));
    }

    public SearchCriteria sanitize(SearchCriteria criteria) {
        if (criteria.limit() < 1 || criteria.limit() > 50) {
            throw new ValidationException("La limite doit être comprise entre 1 et 50");
        }
        if (criteria.offset() < 0 || criteria.offset() > 1_000) {
            throw new ValidationException("Le décalage demandé est invalide");
        }
        String term = singleLine(criteria.term(), "Le terme", 80, false);
        String locale = criteria.locale();
        if (locale != null && !locale.isBlank() && !LOCALE.matcher(locale.trim()).matches()) {
            throw new ValidationException("La locale est invalide");
        }
        String normalizedLocale = locale == null || locale.isBlank() ? null : locale.trim();
        return new SearchCriteria(term, criteria.status(), normalizedLocale, criteria.limit(), criteria.offset());
    }

    private static List<String> tags(List<String> raw) {
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }
        if (raw.size() > 8) {
            throw new ValidationException("Huit tags maximum");
        }
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String tag : raw) {
            if (tag == null) {
                throw new ValidationException("Tag invalide");
            }
            String normalized = tag.trim().toLowerCase(Locale.ROOT);
            if (!TAG.matcher(normalized).matches()) {
                throw new ValidationException("Tag invalide");
            }
            unique.add(normalized);
        }
        return List.copyOf(unique);
    }

    private static String singleLine(String value, String label, int max, boolean required) {
        String cleaned = multiline(value, label, max, required);
        if (cleaned != null && (cleaned.indexOf('\n') >= 0 || cleaned.indexOf('\r') >= 0)) {
            throw new ValidationException(label + " doit tenir sur une ligne");
        }
        return cleaned;
    }

    private static String multiline(String value, String label, int max, boolean required) {
        if (value == null || value.isBlank()) {
            if (required) {
                throw new ValidationException(label + " est obligatoire");
            }
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > max) {
            throw new ValidationException(label + " est trop long");
        }
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (c < 0x20 && c != '\n' && c != '\r' && c != '\t') {
                throw new ValidationException(label + " contient des caractères de contrôle");
            }
        }
        return trimmed;
    }
}
