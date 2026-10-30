import type { Article } from "./api";

export const covers = {
  banner: "/covers/banner.jpg",
  servers: "/covers/servers.jpg",
  archive: "/covers/archive.jpg",
  security: "/covers/security.jpg",
  city: "/covers/city.jpg",
  newsroom: "/covers/newsroom.jpg"
} as const;

const pool = [covers.city, covers.servers, covers.archive, covers.security, covers.newsroom];

export function coverFor(article: Pick<Article, "id" | "title" | "summary" | "tags" | "body">): string {
  const haystack = `${article.title} ${article.summary ?? ""} ${article.body} ${article.tags.join(" ")}`.toLowerCase();
  if (/cache|index|perf|lent|serveur/.test(haystack)) {
    return covers.servers;
  }
  if (/jcr|mod[eè]le|archiv|contenu|dossier/.test(haystack)) {
    return covers.archive;
  }
  if (/s[eé]cur|graphql|api|jeton|rest/.test(haystack)) {
    return covers.security;
  }
  if (/suisse|gen[eè]ve|ville|parcours|lac/.test(haystack)) {
    return covers.city;
  }
  const index = [...article.id].reduce((sum, char) => sum + char.charCodeAt(0), 0) % pool.length;
  return pool[index];
}

export function rubricFor(article: Pick<Article, "tags" | "title" | "summary">): string {
  const haystack = `${article.title} ${article.summary ?? ""} ${article.tags.join(" ")}`.toLowerCase();
  if (/cache|index|jcr|graphql|api|tech|s[eé]cur|rest|mod[eè]le/.test(haystack)) {
    return "Sciences-Tech.";
  }
  if (/parcours|soci[eé]t[eé]|culture|ui/.test(haystack)) {
    return "Société";
  }
  if (/suisse|gen[eè]ve|vaud|monde/.test(haystack)) {
    return "Suisse";
  }
  const tag = article.tags[0]?.trim();
  if (!tag || tag.toLowerCase() === "test") {
    return "Info";
  }
  return tag.charAt(0).toUpperCase() + tag.slice(1);
}
