import { spawnSync } from "node:child_process";
import path from "node:path";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");

function run(script, args) {
  const result = spawnSync(process.execPath, [script, ...args], { stdio: "inherit", cwd: root });
  if (result.status !== 0) {
    process.exit(result.status ?? 1);
  }
}

run(path.join(root, "node_modules", "typescript", "bin", "tsc"), ["--noEmit"]);
run(path.join(root, "node_modules", "vite", "bin", "vite.js"), ["build"]);
