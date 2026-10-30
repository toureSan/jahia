package com.ocsin.jaya.app;

import com.ocsin.jaya.application.EditorialService;
import com.ocsin.jaya.application.SaveArticle;
import com.ocsin.jaya.application.SearchCriteria;
import com.ocsin.jaya.domain.Article;
import com.ocsin.jaya.domain.Principal;

import java.util.List;

/**
 * Jeu de contenus local pour prendre en main l'atelier. Il ne recrée rien si le dépôt contient déjà des articles.
 */
public final class ContentSeeder {

    public void seedIfEmpty(EditorialService editorial) {
        Principal system = Principal.system();
        if (editorial.search(system, new SearchCriteria(null, null, null, 1, 0)).total() > 0) {
            return;
        }
        publish(editorial, system,
                "Faire évoluer un modèle JCR",
                "Ajouter des propriétés sans réécrire les contenus déjà en ligne.",
                "La définition jaya:article grandit par un CND versionné. "
                        + "La migration renseigne les champs d'index manquants, puis les requêtes s'appuient dessus.",
                List.of("jcr", "migration"));
        publish(editorial, system,
                "Cache, index et diagnostic des lenteurs",
                "Mesurer avant de deviner.",
                "Les lectures publiques passent par un cache invalidé à l'écriture. "
                        + "Les filtres de statut et de date ont un index de propriétés. "
                        + "Le plan de la requête lente est journalisé.",
                List.of("performance", "index"));
        editorial.save(system, new SaveArticle(
                null,
                "Sécuriser GraphQL et REST",
                "Brouillon : ne pas publier tant que la revue n'est pas faite.",
                "Jetons à durée de démonstration, droits séparés, requêtes liées, "
                        + "plafond de complexité GraphQL et en-têtes HTTP restrictifs.",
                "fr",
                List.of("securite", "api")));
    }

    private static void publish(EditorialService editorial, Principal actor, String title, String summary, String body, List<String> tags) {
        Article draft = editorial.save(actor, new SaveArticle(null, title, summary, body, "fr", tags));
        editorial.submit(actor, draft.id());
        editorial.publish(actor, draft.id());
    }
}
