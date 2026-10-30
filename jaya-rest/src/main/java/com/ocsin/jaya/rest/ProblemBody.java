package com.ocsin.jaya.rest;

import com.ocsin.jaya.domain.DomainException;

public final class ProblemBody {

    private final String type;
    private final String title;
    private final int status;
    private final String detail;
    private final String code;

    private ProblemBody(String type, String title, int status, String detail, String code) {
        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.code = code;
    }

    public static ProblemBody of(DomainException exception) {
        return new ProblemBody(
                "urn:jaya:error:" + exception.code(),
                titleFor(exception.status()),
                exception.status(),
                exception.getMessage(),
                exception.code());
    }

    public static ProblemBody internal() {
        return new ProblemBody("urn:jaya:error:internal", "Erreur interne", 500, "Erreur interne", "internal");
    }

    public static ProblemBody payloadTooLarge() {
        return new ProblemBody(
                "urn:jaya:error:payload",
                "Requête trop volumineuse",
                413,
                "Le corps de la requête dépasse 64 Ko",
                "payload");
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public int getStatus() {
        return status;
    }

    public String getDetail() {
        return detail;
    }

    public String getCode() {
        return code;
    }

    private static String titleFor(int status) {
        return switch (status) {
            case 400 -> "Requête invalide";
            case 401 -> "Authentification requise";
            case 403 -> "Action non autorisée";
            case 404 -> "Introuvable";
            case 409 -> "Conflit d'état";
            case 429 -> "Trop de tentatives";
            case 502 -> "Système externe indisponible";
            default -> "Erreur";
        };
    }
}
