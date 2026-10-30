import DOMPurify from "dompurify";
import { loadTokens, saveArticle, searchArticles, searchPublished, transition, type Article } from "./api";
import { STORIES, type Story } from "./stories";

export interface NewsStory {
  id: string;
  title: string;
  summary: string;
  html: string;
  rubric: string;
  place: string;
  desk: "info" | "sport" | "culture" | "audio";
  topic: string;
  image: string;
  video?: string;
  publishedAt: string;
  author: string;
}

const RUBRICS: Record<string, string> = {
  suisse: "Suisse",
  monde: "Monde",
  economie: "Economie",
  societe: "Société",
  sciences: "Sciences-Tech.",
  environnement: "Environnement",
  sport: "Sport",
  culture: "Culture",
  audio: "Audio"
};

const PLACES: Record<string, string> = {
  lausanne: "Lausanne",
  geneve: "Genève",
  epesses: "Epesses",
  malley: "Malley",
  nyon: "Nyon",
  renens: "Renens",
  bienne: "Bienne"
};

const TOPIC_TAGS: Record<Story["topic"], string> = {
  Suisse: "suisse",
  Monde: "monde",
  Economie: "economie",
  Société: "societe",
  "Sciences-Tech.": "sciences",
  Environnement: "environnement",
  Sport: "sport",
  Culture: "culture"
};

let publishing: Promise<void> | null = null;

export function ensurePublishedNews(): Promise<void> {
  if (!publishing) {
    publishing = publishMissing().catch((error: unknown) => {
      publishing = null;
      throw error;
    });
  }
  return publishing;
}

export async function loadPublishedNews(): Promise<NewsStory[]> {
  await ensurePublishedNews();
  const result = await searchPublished("");
  const seen = new Set<string>();
  return result.items.filter((article) => article.tags.includes("une")).filter((article) => {
    if (seen.has(article.title)) {
      return false;
    }
    seen.add(article.title);
    return true;
  }).map(toNews);
}

export function toNews(article: Article): NewsStory {
  const placeTag = article.tags.find((tag) => tag.startsWith("lieu-"))?.slice(5) ?? "";
  const rubricTag = article.tags.find((tag) => RUBRICS[tag]);
  const desk = article.tags.includes("audio")
    ? "audio"
    : article.tags.includes("sport")
      ? "sport"
      : article.tags.includes("culture")
        ? "culture"
        : "info";
  return {
    id: article.id,
    title: article.title,
    summary: article.summary ?? "",
    html: article.body,
    rubric: rubricTag ? RUBRICS[rubricTag] : "Info",
    place: PLACES[placeTag] ?? placeTag,
    desk,
    topic: rubricTag ? RUBRICS[rubricTag] : "Info",
    image: attribute(article.body, "img", "src") ?? "/covers/city.jpg",
    video: attribute(article.body, "video", "src"),
    publishedAt: article.updatedAt,
    author: "Rédaction"
  };
}

export function sanitizeNews(html: string): string {
  const source = html.includes("<") ? html : `<p>${escapeHtml(html)}</p>`;
  return DOMPurify.sanitize(source, {
    ADD_TAGS: ["video"],
    ADD_ATTR: ["controls", "poster", "playsinline"]
  });
}

async function publishMissing(): Promise<void> {
  const tokens = await loadTokens();
  const existing = await searchArticles(tokens.admin, "");
  const titles = new Set(existing.items.map((article) => article.title));
  for (const story of STORIES) {
    if (titles.has(story.title)) {
      continue;
    }
    const again = await searchArticles(tokens.admin, story.title);
    if (again.items.some((article) => article.title === story.title)) {
      continue;
    }
    const saved = await saveArticle(tokens.admin, {
      title: clip(story.title, 180),
      summary: clip(story.summary, 280),
      body: storyHtml(story),
      locale: "fr",
      tags: storyTags(story)
    });
    await transition(tokens.admin, saved.id, "submit");
    await transition(tokens.admin, saved.id, "publish");
  }
}

function storyHtml(story: Story): string {
  const image = `<img src="${story.image}" alt="">`;
  const video = story.video
    ? `<video src="${story.video}" poster="${story.image}" controls playsinline></video>`
    : "";
  const paragraphs = story.paragraphs.map((paragraph) => `<p>${escapeHtml(paragraph)}</p>`).join("");
  return `${image}${video}${paragraphs}`;
}

function storyTags(story: Story): string[] {
  const tags = ["une", TOPIC_TAGS[story.topic]];
  if (story.desk === "sport" || story.desk === "culture" || story.desk === "audio") {
    tags.push(story.desk);
  }
  if (story.video) {
    tags.push("video");
  }
  tags.push(`lieu-${slug(story.place)}`);
  return [...new Set(tags)];
}

function attribute(html: string, tag: string, name: string): string | undefined {
  const match = new RegExp(`<${tag}\\b[^>]*\\b${name}=["']([^"']+)["']`, "i").exec(html);
  return match?.[1];
}

function slug(value: string): string {
  return value
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-|-$/g, "");
}

function clip(value: string, max: number): string {
  if (value.length <= max) {
    return value;
  }
  return `${value.slice(0, max - 1).trimEnd()}…`;
}

function escapeHtml(value: string): string {
  return value
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;");
}
