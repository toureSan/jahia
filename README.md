# Jahia

Atelier éditorial pour s'exercer au métier de développeur Jahia : modules OSGi, modèle JCR, API GraphQL et REST, performance, sécurité et livraison.

Le serveur local tourne sur Apache Jackrabbit Oak, donc `mvn verify` et `java -jar` fonctionnent sans licence Jahia. Le module `jaya-bundle` est le paquet OSGi à déposer dans Jahia DX : il publie le service éditorial, le CND et une vue JSP. La session Jahia se branche dans `JahiaSessionProvider`.

## Ce que couvre le dépôt

| Attente | Où |
| --- | --- |
| Maintenance et évolution des modules | Réacteur Maven, frontières nettes, bundle OSGi |
| Modules Java OSGi | `jaya-bundle`, descripteur Declarative Services |
| Repository JCR et modèles | `jaya-jcr` : CND v1 vers v2, migration, index |
| GraphQL, REST, systèmes externes | `jaya-graphql`, `jaya-rest`, client HTTP avec retries |
| Cache, recherche, indexation, lenteurs | Caffeine, JCR-SQL2, index Oak, `GET /api/v1/diagnostics` |
| Sécurité, déploiements, qualité | Jetons, droits, OWASP, tests, Docker, GitHub Actions |
| Front (plus) | `jaya-ui` en React et TypeScript |

## Architecture

```text
jaya-ui  -->  REST / GraphQL  -->  EditorialService  -->  JCR
                                      |        |
                                      cache    système externe
```

Le domaine ne connaît ni JCR ni HTTP. Les cas d'usage reçoivent un `Principal` et une action. Oak ou Jahia ne sont que des fournisseurs de session.

Workflow : `DRAFT` → `IN_REVIEW` → `PUBLISHED`. Une modification d'un article publié le repasse en brouillon. La publication locale est conservée si le système externe échoue ; `syncError` porte la cause et `POST /sync` réessaie.

## Prérequis

- JDK 17 ou plus récent
- Maven 3.8.6 ou plus récent
- Node.js 20 ou plus récent pour la console

## Démarrage

```powershell
mvn verify
.\scripts\run.ps1
```

Dans un second terminal :

```powershell
cd jaya-ui
npm install
npm run dev
```

Ouvrir http://127.0.0.1:5173. La page d’accueil liste les articles publiés, sans jeton. L’atelier est sur http://127.0.0.1:5173/#/atelier. Le serveur écoute par défaut sur http://127.0.0.1:8080. Les jetons de démonstration sont écrits dans le journal au démarrage, et l’atelier les charge via `GET /api/v1/dev/tokens`.

Si Maven est configuré avec un miroir interne injoignable :

```powershell
mvn -s build/maven-central-settings.xml verify
```

Docker, pour un poste qui doit exposer le port :

```powershell
docker compose up --build
```

Le conteneur lie `0.0.0.0`. En local, le processus reste sur `127.0.0.1`.

## API

| Méthode | Chemin | Rôle |
| --- | --- | --- |
| GET | `/api/v1/health` | public |
| GET | `/api/v1/articles?q=&status=&locale=&limit=&offset=` | lecteur : publiés seulement |
| GET | `/api/v1/articles/{id}` | lecteur, 404 si brouillon |
| POST | `/api/v1/articles` | éditeur |
| POST | `/api/v1/articles/{id}/submit` | éditeur |
| POST | `/api/v1/articles/{id}/publish` | publisher |
| POST | `/api/v1/articles/{id}/reject` | publisher |
| POST | `/api/v1/articles/{id}/sync` | publisher |
| POST | `/graphql` | mêmes droits, complexité bornée |
| GET | `/api/v1/diagnostics` | admin |

Les erreurs suivent `application/problem+json` et ne renvoient pas de pile d'appels.

Exemple :

```http
POST /api/v1/articles
Authorization: Bearer <jeton éditeur>
Content-Type: application/json

{"title":"Notes de migration","body":"Ajouter jaya:summary.","locale":"fr","tags":["jcr"]}
```

Recherche GraphQL :

```graphql
query {
  search(term: "migration", limit: 10) {
    total
    items { id title status }
  }
}
```

## Modèle JCR

Le type portable est `jaya:article` (`jaya-jcr/src/main/resources/META-INF/definitions.cnd`). Jahia charge ce fichier depuis le bundle. Les champs `titleLower` et `bodyLower` servent aux recherches `LIKE` sans concaténer la saisie utilisateur : la valeur est un paramètre lié. Oak n'accepte pas la clause `ESCAPE` et traite `\` comme échappement, donc `%`, `_` et `\` sont préfixés par `\`.

`ContentModelMigrator` sait enregistrer une v1 puis la v2, et complète `bodyLower` sur les nœuds déjà présents. Dans Jahia, le CND est la voie normale ; le code ne fait que le rattrapage, au premier appel dans une requête, via `JCRSessionFactory.getCurrentUserSession()`.

Pour le studio Jahia, un exemple qui étend `jnt:content` est dans `jaya-bundle/src/main/resources/jahia/definitions-jahia.cnd`. Il n'est pas activé tel quel : Oak ne connaît pas les types `jnt:*`. La vue `jaya_article/html/article.jsp` encode le titre avec `c:out`.

Les index de propriétés (`jayaStatus`, `jayaLocale`, `jayaUpdatedAt`, `jayaId`, `jayaTitleLower`) sont créés sous `/oak:index` en mode autonome. On ne les écrit pas automatiquement dans un repository Jahia partagé.

## Sécurité

- Jeton Bearer, comparaison après SHA-256, longueur minimale 32.
- Pas de jeton en production si `JAYA_DEV_TOKENS=false` et que les variables `JAYA_TOKEN_*` manquent : le processus refuse de démarrer.
- Un brouillon ressemble à une ressource absente pour un lecteur.
- Le cache public et le cache éditorial n'ont pas les mêmes clés.
- Plafond GraphQL : profondeur 8, complexité 80.
- Corps de requête limité à 64 Ko, redirections externes non suivies, URL externe limitée à http(s).
- Échecs d'authentification limités par adresse.
- En-têtes `nosniff`, `DENY`, `no-store`, CSP `default-src 'none'`.
- Journal d'audit : utilisateur, action, identifiant, résultat. Jamais le corps ni le jeton.

Variables utiles : `JAYA_ENV`, `JAYA_HOST`, `JAYA_PORT`, `JAYA_DATA_DIR` (`memory` pour un dépôt éphémère), `JAYA_DEV_TOKENS`, `JAYA_SEED`, `JAYA_EXTERNAL_URL`, `JAYA_EXTERNAL_TOKEN`, `JAYA_TOKEN_READER`, `JAYA_TOKEN_EDITOR`, `JAYA_TOKEN_PUBLISHER`, `JAYA_TOKEN_ADMIN`.

## Performance

Chaque cas d'usage est chronométré. Au-delà du seuil (`JAYA_SLOW_THRESHOLD_MS`, 50 ms), la recherche JCR journalise son plan `explain`. Le diagnostic admin expose le taux de cache, les moyennes et les opérations lentes. Les écritures invalident les préfixes `article:` et `search:`.

## Qualité

```powershell
mvn verify
cd jaya-ui
npm run build
```

La CI GitHub Actions enchaîne les deux. Avant une revue : pas de requête JCR construite par concaténation, pas de session Jahia fermée par le module, pas de secret dans les journaux, test du rôle lecteur sur tout nouvel état de contenu.

## Limites assumées

Le dépôt embarqué n'est pas un cluster Jahia. Les ACL JCR fines du studio, le rendu des vues et le bus OSGi se vérifient dans une instance DX. Le cache est local au processus.
