import { useEditor, EditorContent } from "@tiptap/react";
import StarterKit from "@tiptap/starter-kit";
import Image from "@tiptap/extension-image";
import Link from "@tiptap/extension-link";
import Placeholder from "@tiptap/extension-placeholder";
import { Node, mergeAttributes } from "@tiptap/core";
import { useEffect } from "react";

const Video = Node.create({
  name: "video",
  group: "block",
  atom: true,
  selectable: true,
  draggable: true,
  addAttributes() {
    return {
      src: { default: null },
      poster: { default: null }
    };
  },
  parseHTML() {
    return [{ tag: "video" }];
  },
  renderHTML({ HTMLAttributes }) {
    return ["video", mergeAttributes(HTMLAttributes, { controls: true, playsinline: true, class: "max-h-64 w-full bg-black" })];
  }
});

const ACTIONS = [
  { id: "bold", label: "Gras" },
  { id: "italic", label: "Italique" },
  { id: "heading", label: "Titre" },
  { id: "bullet", label: "Liste" }
] as const;

export function RichTextField({
  value,
  disabled,
  onChange
}: {
  value: string;
  disabled: boolean;
  onChange: (html: string) => void;
}) {
  const editor = useEditor({
    immediatelyRender: false,
    editable: !disabled,
    extensions: [
      StarterKit.configure({ heading: { levels: [2] } }),
      Link.configure({ openOnClick: false, autolink: true }),
      Image.configure({ allowBase64: false }),
      Video,
      Placeholder.configure({ placeholder: "Écrivez le sujet, ajoutez une image ou une vidéo…" })
    ],
    content: value || "<p></p>",
    onUpdate: ({ editor: current }) => onChange(current.getHTML()),
    editorProps: {
      attributes: {
        class: "min-h-64 px-3 py-2 text-sm outline-none [&_h2]:mt-3 [&_h2]:text-lg [&_h2]:font-bold [&_img]:my-2 [&_img]:max-h-48 [&_li]:ml-5 [&_li]:list-disc [&_p]:my-2 [&_video]:my-2 [&_video]:max-h-48"
      }
    }
  });

  useEffect(() => {
    editor?.setEditable(!disabled);
  }, [disabled, editor]);

  function run(id: (typeof ACTIONS)[number]["id"]) {
    if (!editor) {
      return;
    }
    const chain = editor.chain().focus();
    if (id === "bold") {
      chain.toggleBold().run();
    } else if (id === "italic") {
      chain.toggleItalic().run();
    } else if (id === "heading") {
      chain.toggleHeading({ level: 2 }).run();
    } else {
      chain.toggleBulletList().run();
    }
  }

  function addImage() {
    const src = window.prompt("Adresse de l'image (https://… ou /covers/city.jpg)");
    if (!src || !editor) {
      return;
    }
    editor.chain().focus().setImage({ src: src.trim() }).run();
  }

  function addVideo() {
    const src = window.prompt("Adresse de la vidéo (.mp4)", "/videos/reportage.mp4");
    if (!src || !editor) {
      return;
    }
    editor.chain().focus().insertContent({ type: "video", attrs: { src: src.trim(), poster: "" } }).run();
  }

  return (
    <div className={`overflow-hidden rounded-lg border bg-background ${disabled ? "opacity-60" : ""}`}>
      <div className="flex flex-wrap gap-1 border-b bg-muted/40 p-1">
        {ACTIONS.map((action) => (
          <button
            key={action.id}
            type="button"
            disabled={disabled}
            className="rounded-md px-2 py-1 text-xs font-semibold hover:bg-background"
            onClick={() => run(action.id)}
          >
            {action.label}
          </button>
        ))}
        <button type="button" disabled={disabled} className="rounded-md px-2 py-1 text-xs font-semibold hover:bg-background" onClick={addImage}>
          Image
        </button>
        <button type="button" disabled={disabled} className="rounded-md px-2 py-1 text-xs font-semibold hover:bg-background" onClick={addVideo}>
          Vidéo
        </button>
      </div>
      <EditorContent editor={editor} />
    </div>
  );
}
