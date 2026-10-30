import { StrictMode, useEffect, useState } from "react";
import { createRoot } from "react-dom/client";
import { App } from "./App";
import { Landing } from "./Landing";
import "./index.css";

function Root() {
  const [hash, setHash] = useState(window.location.hash);

  useEffect(() => {
    const sync = () => setHash(window.location.hash);
    window.addEventListener("hashchange", sync);
    return () => window.removeEventListener("hashchange", sync);
  }, []);

  return hash.startsWith("#/atelier") ? <App /> : <Landing />;
}

const root = document.getElementById("root");
if (!root) {
  throw new Error("Point de montage introuvable");
}
createRoot(root).render(
  <StrictMode>
    <Root />
  </StrictMode>
);
