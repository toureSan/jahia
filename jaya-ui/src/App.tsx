import { FormEvent, KeyboardEvent, useEffect, useState } from "react";
import { Search } from "lucide-react";
import {
  ApiError,
  Article,
  DevTokens,
  Diagnostics,
  Role,
  diagnostics,
  loadTokens,
  saveArticle,
  searchArticles,
  transition
} from "./api";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { RichTextField } from "./RichTextField";
import { ensurePublishedNews } from "./news";

const ROLES: { id: Role; label: string }[] = [
  { id: "reader", label: "Lecteur" },
  { id: "editor", label: "Éditeur" },
  { id: "publisher", label: "Publisher" },
  { id: "admin", label: "Admin" }
];

const STATUS: Record<Article["status"], string> = {
  DRAFT: "Brouillon",
  IN_REVIEW: "En relecture",
  PUBLISHED: "Publié"
};

const EMPTY = { title: "", summary: "", body: "", tags: "" };

function statusVariant(status: Article["status"]): "default" | "secondary" | "outline" {
  if (status === "PUBLISHED") {
    return "default";
  }
  if (status === "IN_REVIEW") {
    return "outline";
  }
  return "secondary";
}

export function App() {
  const [tokens, setTokens] = useState<DevTokens | null>(null);
  const [role, setRole] = useState<Role>("admin");
  const [term, setTerm] = useState("");
  const [articles, setArticles] = useState<Article[]>([]);
  const [selected, setSelected] = useState<Article | null>(null);
  const [form, setForm] = useState(EMPTY);
  const [view, setView] = useState<"desk" | "diagnostics">("desk");
  const [report, setReport] = useState<Diagnostics | null>(null);
  const [message, setMessage] = useState("Connexion au serveur local…");

  useEffect(() => {
    let cancelled = false;
    loadTokens()
      .then(async (loaded) => {
        await ensurePublishedNews();
        if (!cancelled) {
          setTokens(loaded);
          setMessage("Jetons de démonstration chargés.");
        }
      })
      .catch(() => {
        if (!cancelled) {
          setMessage("Le serveur Jahia ne répond pas. Lancez-le sur le port 8080.");
        }
      });
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    if (!tokens) {
      return;
    }
    setSelected(null);
    setForm(EMPTY);
    void refresh(tokens[role], term);
  }, [tokens, role]);

  async function refresh(token: string, query: string) {
    try {
      const result = await searchArticles(token, query);
      setArticles(result.items.filter((article) => !article.tags.includes("doublon")));
      setMessage(`${result.total} article(s) visible(s) pour ce rôle.`);
    } catch (error) {
      setMessage(error instanceof ApiError ? error.message : "Recherche impossible");
    }
  }

  function token(): string {
    if (!tokens) {
      throw new ApiError("Jetons indisponibles", 401);
    }
    return tokens[role];
  }

  async function onSearch(event: FormEvent) {
    event.preventDefault();
    await refresh(token(), term);
  }

  function select(article: Article) {
    setSelected(article);
    setForm({
      title: article.title,
      summary: article.summary ?? "",
      body: article.body,
      tags: article.tags.join(", ")
    });
  }

  function resetForm() {
    setSelected(null);
    setForm(EMPTY);
  }

  async function onSave(event: FormEvent) {
    event.preventDefault();
    try {
      const saved = await saveArticle(token(), {
        id: selected?.id,
        title: form.title,
        summary: form.summary || null,
        body: form.body,
        locale: "fr",
        tags: form.tags.split(",").map((tag) => tag.trim()).filter(Boolean)
      });
      setSelected(saved);
      setMessage(`« ${saved.title} » enregistré (${STATUS[saved.status]}).`);
      await refresh(token(), term);
    } catch (error) {
      setMessage(error instanceof ApiError ? error.message : "Enregistrement impossible");
    }
  }

  async function act(action: "submit" | "publish" | "reject" | "sync") {
    if (!selected) {
      return;
    }
    try {
      const updated = await transition(token(), selected.id, action);
      setSelected(updated);
      setMessage(`Action ${action} appliquée. Statut : ${STATUS[updated.status]}.`);
      await refresh(token(), term);
    } catch (error) {
      setMessage(error instanceof ApiError ? error.message : "Action impossible");
    }
  }

  async function openDiagnostics() {
    setView("diagnostics");
    try {
      setReport(await diagnostics(token()));
    } catch (error) {
      setReport(null);
      setMessage(error instanceof ApiError ? error.message : "Diagnostic indisponible");
    }
  }

  const canEdit = role === "editor" || role === "publisher" || role === "admin";
  const canPublish = role === "publisher" || role === "admin";

  return (
    <div className="min-h-svh bg-background">
      <header className="border-b bg-background/90">
        <div className="mx-auto flex max-w-6xl flex-wrap items-end justify-between gap-4 px-4 py-5">
          <div>
            <p className="text-sm font-medium tracking-wide text-primary uppercase">Atelier éditorial</p>
            <h1 className="font-heading text-4xl tracking-tight">Jahia</h1>
          </div>
          <p className="max-w-md text-sm text-muted-foreground">{message}</p>
          <Button variant="outline" asChild>
            <a href="#/">RTS clone</a>
          </Button>
        </div>
      </header>

      <div className="mx-auto grid max-w-6xl gap-6 px-4 py-6 lg:grid-cols-[220px_1fr]">
        <aside className="grid content-start gap-6">
          <div className="grid gap-2">
            <Button variant={view === "desk" ? "default" : "outline"} onClick={() => setView("desk")}>Contenus</Button>
            <Button variant={view === "diagnostics" ? "default" : "outline"} onClick={() => void openDiagnostics()}>
              Diagnostics
            </Button>
          </div>
          <div className="grid gap-2">
            <p className="text-xs font-medium tracking-wide text-muted-foreground uppercase">Rôle</p>
            {ROLES.map((item) => (
              <Button
                key={item.id}
                type="button"
                variant={role === item.id ? "secondary" : "ghost"}
                className="justify-start"
                onClick={() => setRole(item.id)}
              >
                {item.label}
              </Button>
            ))}
          </div>
        </aside>

        {view === "desk" ? (
          <main className="grid gap-4">
            <form className="flex gap-2" onSubmit={(event) => void onSearch(event)}>
              <Input
                value={term}
                onChange={(event) => setTerm(event.target.value)}
                placeholder="Rechercher un titre ou un texte"
                aria-label="Recherche"
              />
              <Button type="submit">
                <Search />
                Rechercher
              </Button>
            </form>
            <div className="grid items-start gap-4 lg:grid-cols-[minmax(220px,0.8fr)_1.2fr]">
              <section className="grid gap-3">
                {articles.length === 0 ? <p className="text-sm text-muted-foreground">Aucun article visible avec ce rôle.</p> : null}
                {articles.map((article) => (
                  <Card
                    key={article.id}
                    role="button"
                    tabIndex={0}
                    className={selected?.id === article.id ? "cursor-pointer ring-2 ring-primary" : "cursor-pointer hover:bg-muted/40"}
                    onClick={() => select(article)}
                    onKeyDown={(event: KeyboardEvent<HTMLDivElement>) => {
                      if (event.key === "Enter" || event.key === " ") {
                        event.preventDefault();
                        select(article);
                      }
                    }}
                  >
                    <CardHeader>
                      <div className="flex flex-wrap gap-2">
                        <Badge variant={statusVariant(article.status)} className="w-fit">{STATUS[article.status]}</Badge>
                        {article.tags.includes("video") || article.body.includes("<video") ? <Badge variant="outline">Vidéo</Badge> : null}
                      </div>
                      <CardTitle>{article.title}</CardTitle>
                      {article.summary ? <CardDescription>{article.summary}</CardDescription> : null}
                    </CardHeader>
                  </Card>
                ))}
              </section>
              <Card>
                <CardHeader>
                  <div className="flex items-center justify-between gap-3">
                    <CardTitle className="text-xl">{selected ? "Révision" : "Nouvel article"}</CardTitle>
                    <Button type="button" variant="outline" onClick={resetForm} disabled={!canEdit}>Nouveau</Button>
                  </div>
                </CardHeader>
                <CardContent>
                  <form className="grid gap-4" onSubmit={(event) => void onSave(event)}>
                    <div className="grid gap-2">
                      <Label htmlFor="title">Titre</Label>
                      <Input id="title" value={form.title} disabled={!canEdit} required onChange={(event) => setForm({ ...form, title: event.target.value })} />
                    </div>
                    <div className="grid gap-2">
                      <Label htmlFor="summary">Résumé</Label>
                      <Input id="summary" value={form.summary} disabled={!canEdit} onChange={(event) => setForm({ ...form, summary: event.target.value })} />
                    </div>
                    <div className="grid gap-2">
                      <Label>Corps</Label>
                      <RichTextField
                        key={selected?.id ?? "nouveau"}
                        value={form.body}
                        disabled={!canEdit}
                        onChange={(html) => setForm((current) => ({ ...current, body: html }))}
                      />
                      <p className="text-xs text-muted-foreground">
                        Gras, titre, liste, image et vidéo. Le tag une affiche l’article sur la une, le tag video le place parmi les vidéos. Enregistrer un article publié le repasse en brouillon : il faut le soumettre puis le publier.
                      </p>
                    </div>
                    <div className="grid gap-2">
                      <Label htmlFor="tags">Tags</Label>
                      <Input id="tags" value={form.tags} disabled={!canEdit} placeholder="une, video, suisse" onChange={(event) => setForm({ ...form, tags: event.target.value })} />
                    </div>
                    {selected?.syncError ? <p className="text-sm text-destructive">{selected.syncError}</p> : null}
                    <div className="flex flex-wrap gap-2">
                      <Button type="submit" disabled={!canEdit}>Enregistrer</Button>
                      <Button type="button" variant="secondary" disabled={!canEdit || !selected || selected.status !== "DRAFT"} onClick={() => void act("submit")}>
                        Soumettre
                      </Button>
                      <Button type="button" variant="secondary" disabled={!canPublish || !selected || selected.status !== "IN_REVIEW"} onClick={() => void act("publish")}>
                        Publier
                      </Button>
                      <Button type="button" variant="outline" disabled={!canPublish || !selected || selected.status !== "IN_REVIEW"} onClick={() => void act("reject")}>
                        Rejeter
                      </Button>
                      <Button type="button" variant="outline" disabled={!canPublish || !selected || selected.status !== "PUBLISHED"} onClick={() => void act("sync")}>
                        Synchroniser
                      </Button>
                    </div>
                  </form>
                </CardContent>
              </Card>
            </div>
          </main>
        ) : (
          <main className="grid gap-4">
            <h2 className="font-heading text-3xl tracking-tight">Performance</h2>
            {report ? (
              <>
                <div className="grid gap-3 sm:grid-cols-3">
                  <Card>
                    <CardHeader>
                      <CardDescription>Hits</CardDescription>
                      <CardTitle className="font-heading text-4xl">{report.cache.hitCount}</CardTitle>
                    </CardHeader>
                  </Card>
                  <Card>
                    <CardHeader>
                      <CardDescription>Miss</CardDescription>
                      <CardTitle className="font-heading text-4xl">{report.cache.missCount}</CardTitle>
                    </CardHeader>
                  </Card>
                  <Card>
                    <CardHeader>
                      <CardDescription>Taux</CardDescription>
                      <CardTitle className="font-heading text-4xl">{Math.round(report.cache.hitRate * 100)}%</CardTitle>
                    </CardHeader>
                  </Card>
                </div>
                <Card>
                  <CardHeader>
                    <CardTitle>Opérations mesurées</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="overflow-x-auto">
                      <table className="w-full text-sm">
                        <thead className="text-left text-muted-foreground">
                          <tr>
                            <th className="py-2 font-medium">Nom</th>
                            <th className="py-2 font-medium">Appels</th>
                            <th className="py-2 font-medium">Moyenne</th>
                          </tr>
                        </thead>
                        <tbody>
                          {report.operations.map((operation) => (
                            <tr key={operation.name} className="border-t">
                              <td className="py-2">{operation.name}</td>
                              <td className="py-2">{operation.count}</td>
                              <td className="py-2">{operation.averageMillis} ms</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </CardContent>
                </Card>
              </>
            ) : <p className="text-sm text-muted-foreground">Diagnostic réservé à l'administrateur.</p>}
          </main>
        )}
      </div>
    </div>
  );
}
