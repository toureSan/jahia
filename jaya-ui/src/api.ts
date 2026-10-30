export type Role = "reader" | "editor" | "publisher" | "admin";

export interface DevTokens {
  reader: string;
  editor: string;
  publisher: string;
  admin: string;
}

export interface Article {
  id: string;
  title: string;
  summary: string | null;
  body: string;
  status: "DRAFT" | "IN_REVIEW" | "PUBLISHED";
  locale: string;
  tags: string[];
  updatedAt: string;
  updatedBy: string;
  externalId: string | null;
  syncError: string | null;
}

export interface SearchResult {
  total: number;
  items: Article[];
}

export interface Diagnostics {
  cache: {
    hitCount: number;
    missCount: number;
    hitRate: number;
    evictionCount: number;
  };
  slowOperations: { name: string; durationMillis: number; at: string }[];
  operations: { name: string; count: number; averageMillis: number }[];
}

export class ApiError extends Error {
  constructor(message: string, readonly status: number) {
    super(message);
  }
}

async function request<T>(path: string, token: string | null, init?: RequestInit): Promise<T> {
  const headers = new Headers(init?.headers);
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }
  if (init?.body) {
    headers.set("Content-Type", "application/json");
  }
  const response = await fetch(path, { ...init, headers });
  if (!response.ok) {
    const problem = await response.json().catch(() => ({ detail: response.statusText }));
    throw new ApiError(problem.detail ?? "Requête refusée", response.status);
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return response.json() as Promise<T>;
}

export function loadTokens(): Promise<DevTokens> {
  return request<DevTokens>("/api/v1/dev/tokens", null);
}

export function searchPublished(term: string): Promise<SearchResult> {
  return searchArticles(null, term);
}

export function searchArticles(token: string | null, term: string): Promise<SearchResult> {
  const query = `
    query Search($term: String) {
      search(term: $term, limit: 40) {
        total
        items { id title summary body status locale tags updatedAt updatedBy externalId syncError }
      }
    }
  `;
  return request<{ data?: { search: SearchResult }; errors?: { message: string }[] }>("/graphql", token, {
    method: "POST",
    body: JSON.stringify({ query, variables: { term: term || null } })
  }).then((payload) => {
    if (payload.errors?.length) {
      throw new ApiError(payload.errors[0].message, 400);
    }
    return payload.data?.search ?? { total: 0, items: [] };
  });
}

export function saveArticle(token: string, article: Partial<Article> & { title: string; body: string }): Promise<Article> {
  return request<Article>("/api/v1/articles", token, {
    method: "POST",
    body: JSON.stringify({
      id: article.id,
      title: article.title,
      summary: article.summary,
      body: article.body,
      locale: article.locale ?? "fr",
      tags: article.tags ?? []
    })
  });
}

export function transition(token: string, id: string, action: "submit" | "publish" | "reject" | "sync"): Promise<Article> {
  return request<Article>(`/api/v1/articles/${id}/${action}`, token, { method: "POST" });
}

export function diagnostics(token: string): Promise<Diagnostics> {
  return request<Diagnostics>("/api/v1/diagnostics", token);
}
