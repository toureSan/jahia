import { FormEvent, KeyboardEvent, useEffect, useState } from "react";
import { Menu, Play, Search, X } from "lucide-react";
import { ApiError } from "./api";
import { loadPublishedNews, sanitizeNews, type NewsStory as Story } from "./news";

const SECTIONS = [
  { id: "info", label: "Info" },
  { id: "sport", label: "Sport" },
  { id: "culture", label: "Culture" },
  { id: "tv", label: "TV & Streaming" },
  { id: "audio", label: "Audio" }
] as const;

const TOPICS = ["À la une", "Suisse", "Monde", "Economie", "Société", "Sciences-Tech.", "Environnement"] as const;

type SectionId = (typeof SECTIONS)[number]["id"];

function formatDate(value: string): string {
  const parsed = new Date(value);
  const date = Number.isNaN(parsed.getTime()) ? epoch(value) : parsed;
  if (Number.isNaN(date.getTime())) {
    return "";
  }
  return new Intl.DateTimeFormat("fr-FR", { day: "numeric", month: "long", year: "numeric" }).format(date);
}

function epoch(value: string): Date {
  const numeric = Number(value);
  if (!Number.isFinite(numeric)) {
    return new Date(NaN);
  }
  return new Date(numeric < 1e12 ? numeric * 1000 : numeric);
}

function matches(story: Story, section: SectionId, topic: (typeof TOPICS)[number], query: string): boolean {
  if (query) {
    const haystack = `${story.title} ${story.summary} ${story.place} ${story.rubric} ${story.html}`.toLowerCase();
    return haystack.includes(query);
  }
  if (section === "tv") {
    return Boolean(story.video);
  }
  if (section === "audio") {
    return story.desk === "audio";
  }
  if (section === "sport") {
    return story.desk === "sport";
  }
  if (section === "culture") {
    return story.desk === "culture";
  }
  if (topic === "À la une") {
    return true;
  }
  return story.topic === topic;
}

function activate(article: Story, open: (article: Story) => void) {
  return {
    role: "link" as const,
    tabIndex: 0,
    onClick: () => open(article),
    onKeyDown: (event: KeyboardEvent) => {
      if (event.key === "Enter" || event.key === " ") {
        event.preventDefault();
        open(article);
      }
    }
  };
}

export function Landing() {
  const [term, setTerm] = useState("");
  const [query, setQuery] = useState("");
  const [stories, setStories] = useState<Story[]>([]);
  const [message, setMessage] = useState("Chargement des publications…");
  const [reading, setReading] = useState<Story | null>(null);
  const [section, setSection] = useState<SectionId>("info");
  const [topic, setTopic] = useState<(typeof TOPICS)[number]>("À la une");
  const [menuOpen, setMenuOpen] = useState(false);

  useEffect(() => {
    document.title = reading ? `${reading.title} — RTS clone` : "RTS clone";
  }, [reading]);

  useEffect(() => {
    let cancelled = false;
    loadPublishedNews()
      .then((loaded) => {
        if (!cancelled) {
          setStories(loaded);
          setMessage(loaded.length === 0 ? "Aucun sujet publié." : "");
        }
      })
      .catch((error: unknown) => {
        if (!cancelled) {
          setStories([]);
          setMessage(error instanceof ApiError ? error.message : "Le serveur Jahia ne répond pas. Lancez-le sur le port 8080.");
        }
      });
    return () => {
      cancelled = true;
    };
  }, []);

  function chooseSection(next: SectionId) {
    setSection(next);
    setReading(null);
    setMenuOpen(false);
    setTopic("À la une");
    setQuery("");
    setTerm("");
  }

  function onSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const next = String(new FormData(event.currentTarget).get("q") ?? "").trim();
    setTerm(next);
    setReading(null);
    setQuery(next);
  }

  const visible = stories.filter((story) => matches(story, section, topic, query.toLowerCase()));
  const [featured, ...rest] = visible;

  return (
    <div className="min-h-svh bg-white text-neutral-950">
      <header className="sticky top-0 z-20 border-b border-neutral-200 bg-white">
        <div className="mx-auto flex max-w-6xl items-center gap-4 px-4 py-3">
          <button type="button" className="lg:hidden" aria-label="Ouvrir le menu" onClick={() => setMenuOpen((open) => !open)}>
            {menuOpen ? <X /> : <Menu />}
          </button>
          <a
            href="#/"
            className="flex items-center gap-2"
            aria-label="RTS clone"
            onClick={() => {
              setReading(null);
              chooseSection("info");
            }}
          >
            <span className="grid h-9 w-9 place-items-center bg-[#e30613] text-[13px] leading-none font-black tracking-[-0.08em] text-white italic">
              RTS
            </span>
            <span className="text-2xl leading-none font-black tracking-tight">clone</span>
          </a>
          <nav className="ml-4 hidden items-center gap-5 text-[15px] font-semibold lg:flex" aria-label="Rubriques">
            {SECTIONS.map((item) => (
              <button
                key={item.id}
                type="button"
                className={item.id === section ? "text-[#e30613]" : "hover:text-[#e30613]"}
                onClick={() => chooseSection(item.id)}
              >
                {item.label}
              </button>
            ))}
          </nav>
          <form className="ml-auto flex min-w-0 items-center gap-2" onSubmit={(event) => void onSearch(event)}>
            <label className="min-w-0 flex-1 sm:flex-none">
              <span className="sr-only">Recherche</span>
              <input
                value={term}
                onChange={(event) => setTerm(event.target.value)}
                name="q"
                placeholder="Rechercher"
                aria-label="Recherche"
                className="h-9 w-full rounded-full border border-neutral-300 bg-neutral-50 px-3 text-sm outline-none focus:border-[#e30613] sm:w-44"
              />
            </label>
            <button type="submit" aria-label="Rechercher" className="grid size-9 shrink-0 place-items-center rounded-full bg-[#e30613] text-white">
              <Search className="size-4" />
            </button>
          </form>
          <a href="#/atelier" className="hidden shrink-0 text-sm font-semibold hover:text-[#e30613] sm:inline">
            Rédaction
          </a>
        </div>
        {menuOpen ? (
          <nav className="grid border-t border-neutral-200 px-4 py-3 lg:hidden" aria-label="Rubriques">
            {SECTIONS.map((item) => (
              <button
                key={item.id}
                type="button"
                className={`py-2 text-left text-base font-semibold ${item.id === section ? "text-[#e30613]" : ""}`}
                onClick={() => chooseSection(item.id)}
              >
                {item.label}
              </button>
            ))}
            <a href="#/atelier" className="py-2 text-base font-semibold">Rédaction</a>
          </nav>
        ) : null}
        {section === "info" && !reading ? (
          <nav className="mx-auto flex max-w-6xl gap-5 overflow-x-auto px-4 text-sm font-semibold [-ms-overflow-style:none] [scrollbar-width:none] [&::-webkit-scrollbar]:hidden" aria-label="Info">
            {TOPICS.map((item) => (
              <button
                key={item}
                type="button"
                className={`shrink-0 border-b-2 py-2 ${item === topic ? "border-[#e30613] text-[#e30613]" : "border-transparent hover:text-[#e30613]"}`}
                onClick={() => {
                  setTopic(item);
                  setReading(null);
                  setQuery("");
                  setTerm("");
                }}
              >
                {item}
              </button>
            ))}
          </nav>
        ) : null}
      </header>

      {reading ? (
        <ArticleView article={reading} onBack={() => setReading(null)} />
      ) : (
        <Home
          featured={featured}
          rest={rest}
          message={message}
          searching={query.length > 0}
          topic={topic}
          onOpen={setReading}
        />
      )}

      <footer className="mt-12 bg-neutral-950 text-neutral-200">
        <div className="mx-auto grid max-w-6xl gap-8 px-4 py-10 sm:grid-cols-3">
          <div>
            <p className="flex items-center gap-2 text-white">
              <span className="grid h-8 w-8 place-items-center bg-[#e30613] text-xs font-black text-white italic">RTS</span>
              <span className="text-lg font-black">clone</span>
            </p>
            <p className="mt-3 max-w-xs text-sm text-neutral-400">
              Articles et vidéos de démonstration. Ce n’est pas le site de la RTS.
            </p>
          </div>
          <div className="grid gap-2 text-sm">
            {SECTIONS.map((item) => (
              <button key={item.id} type="button" className="w-fit hover:text-white" onClick={() => chooseSection(item.id)}>
                {item.label}
              </button>
            ))}
          </div>
          <div className="text-sm">
            <a href="#/atelier" className="hover:text-white">Rédaction</a>
          </div>
        </div>
      </footer>
    </div>
  );
}

function Home({
  featured,
  rest,
  message,
  searching,
  topic,
  onOpen
}: {
  featured: Story | undefined;
  rest: Story[];
  message: string;
  searching: boolean;
  topic: string;
  onOpen: (article: Story) => void;
}) {
  if (!featured) {
    return (
      <main className="mx-auto max-w-6xl px-4 py-16">
        <p className="text-lg text-neutral-600">{message || `Aucun sujet dans ${topic}.`}</p>
      </main>
    );
  }

  const banner = rest.find((story) => story.video) ?? rest[0];
  const videos = rest.filter((story) => story.video);

  return (
    <main>
      {!searching ? <Ticker articles={[featured, ...rest]} onOpen={onOpen} /> : null}
      <div className="mx-auto grid max-w-6xl gap-8 px-4 py-6 lg:grid-cols-[minmax(0,1.7fr)_minmax(260px,0.8fr)]">
        <article className="group cursor-pointer" {...activate(featured, onOpen)}>
          <Media story={featured} className="aspect-video w-full" />
          <p className="mt-3 text-sm font-bold tracking-wide text-[#e30613] uppercase">{featured.rubric} · {featured.place}</p>
          <h1 className="mt-1 text-3xl leading-[1.05] font-extrabold tracking-tight group-hover:underline sm:text-5xl">
            {featured.title}
          </h1>
          <p className="mt-3 max-w-2xl text-lg text-neutral-600">{featured.summary}</p>
        </article>
        <aside>
          <h2 className="flex items-center gap-2 pb-2 text-base font-extrabold"><span className="h-4 w-1 bg-[#e30613]" />À lire aussi</h2>
          <div className="divide-y divide-neutral-200">
            {rest.slice(0, 4).map((article) => (
              <article key={article.id} className="flex cursor-pointer gap-3 py-3" {...activate(article, onOpen)}>
                <Media story={article} className="h-20 w-28 shrink-0" />
                <div>
                  <p className="text-xs font-bold text-[#e30613] uppercase">{article.rubric}</p>
                  <h3 className="font-bold leading-snug hover:underline">{article.title}</h3>
                </div>
              </article>
            ))}
            {rest.length === 0 ? <p className="py-4 text-sm text-neutral-500">Pas d’autre sujet pour l’instant.</p> : null}
          </div>
        </aside>
      </div>

      {!searching && banner ? (
        <section className="mx-auto max-w-6xl px-4">
          <article className="relative cursor-pointer overflow-hidden" {...activate(banner, onOpen)}>
            <Media story={banner} className="aspect-[21/9] w-full" />
            <div className="pointer-events-none absolute inset-0 bg-gradient-to-r from-black/80 via-black/45 to-black/10" />
            <div className="absolute inset-0 flex max-w-xl flex-col justify-end p-6 text-white sm:p-10">
              <p className="text-sm font-bold tracking-wide text-[#ff8a8a] uppercase">
                {banner.video ? "Vidéo" : "Dossier"}
              </p>
              <h2 className="mt-2 text-3xl leading-none font-extrabold tracking-tight sm:text-5xl">{banner.title}</h2>
              <p className="mt-3 line-clamp-3 text-white/85">{banner.summary}</p>
            </div>
          </article>
        </section>
      ) : null}

      {videos.length > 0 && !searching ? (
        <section className="mx-auto max-w-6xl px-4 py-10">
          <h2 className="flex items-center gap-2 text-lg font-extrabold"><span className="h-5 w-1 bg-[#e30613]" />En vidéo</h2>
          <div className="mt-4 grid gap-x-5 gap-y-8 sm:grid-cols-2 lg:grid-cols-3">
            {videos.map((article) => (
              <article key={article.id} className="cursor-pointer" {...activate(article, onOpen)}>
                <Media story={article} className="aspect-video w-full" />
                <p className="mt-2 text-[13px] font-bold text-[#e30613]">{article.rubric}</p>
                <h3 className="text-[17px] leading-snug font-bold hover:underline">{article.title}</h3>
              </article>
            ))}
          </div>
        </section>
      ) : null}

      {rest.length > 0 ? (
        <section className="mx-auto max-w-6xl px-4 py-10">
          <h2 className="flex items-center gap-2 text-lg font-extrabold"><span className="h-5 w-1 bg-[#e30613]" />{searching ? "Résultats" : "À la une"}</h2>
          <div className="mt-4 grid gap-x-5 gap-y-8 sm:grid-cols-2 lg:grid-cols-3">
            {rest.map((article) => (
              <article key={article.id} className="cursor-pointer" {...activate(article, onOpen)}>
                <Media story={article} className="aspect-video w-full" />
                <p className="mt-2 text-[13px] font-bold text-[#e30613]">{article.rubric}</p>
                <h3 className="text-[17px] leading-snug font-bold hover:underline">{article.title}</h3>
              </article>
            ))}
          </div>
        </section>
      ) : null}
    </main>
  );
}

function Media({ story, className }: { story: Story; className?: string }) {
  return (
    <div className={`relative overflow-hidden bg-neutral-200 ${className ?? ""}`}>
      <img src={story.image} alt="" className="h-full w-full object-cover" />
      {story.video ? (
        <>
          <span className="absolute inset-0 grid place-items-center">
            <span className="grid size-11 place-items-center rounded-full bg-white text-[#e30613] shadow-md">
              <Play className="size-5 fill-[#e30613]" />
            </span>
          </span>
          <span className="absolute bottom-2 left-2 bg-black/85 px-1.5 py-0.5 text-[11px] font-semibold text-white">Vidéo</span>
        </>
      ) : null}
    </div>
  );
}

function Ticker({ articles, onOpen }: { articles: Story[]; onOpen: (article: Story) => void }) {
  return (
    <div className="border-b border-neutral-200">
      <div className="mx-auto flex max-w-6xl items-center gap-3 px-4 py-2">
        <span className="shrink-0 bg-[#e30613] px-2 py-1 text-[11px] font-bold tracking-wide text-white uppercase">En continu</span>
        <div className="flex min-w-0 gap-4 overflow-x-auto text-sm font-semibold [-ms-overflow-style:none] [scrollbar-width:none] [&::-webkit-scrollbar]:hidden">
          {articles.slice(0, 6).map((article) => (
            <button key={article.id} type="button" className="shrink-0 hover:text-[#e30613]" onClick={() => onOpen(article)}>
              {article.title}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}

function ArticleView({ article, onBack }: { article: Story; onBack: () => void }) {
  return (
    <article className="mx-auto grid max-w-3xl gap-5 px-4 py-8">
      <button type="button" className="w-fit text-sm font-bold text-[#e30613]" onClick={onBack}>
        {article.rubric}
      </button>
      <p className="text-sm font-bold tracking-wide text-[#e30613] uppercase">{article.rubric} · {article.place}</p>
      <h1 className="text-4xl leading-[1.05] font-extrabold tracking-tight sm:text-5xl">{article.title}</h1>
      <p className="text-xl leading-8 text-neutral-600">{article.summary}</p>
      <p className="text-sm text-neutral-500">
        {article.author} · {formatDate(article.publishedAt)} · article de démonstration
      </p>
      <div className="article-body max-w-prose text-lg leading-8" dangerouslySetInnerHTML={{ __html: sanitizeNews(article.html) }} />
    </article>
  );
}
