const { onRequest } = require("firebase-functions/v2/https");
const { defineSecret, defineString } = require("firebase-functions/params");
const admin = require("firebase-admin");

admin.initializeApp();

const OPENAI_API_KEY = defineSecret("OPENAI_API_KEY");
const GITHUB_TOKEN = defineSecret("GITHUB_TOKEN");
const AI_ALLOWED_EMAILS = defineString("AI_ALLOWED_EMAILS", {
  default: ""
});

const MODEL = "gpt-5.6-luna";

const corsHeaders = {
  "Access-Control-Allow-Origin": "https://iammdtanvirrahman.github.io",
  "Access-Control-Allow-Headers": "Content-Type, Authorization",
  "Access-Control-Allow-Methods": "POST, OPTIONS"
};

function send(res, status, body) {
  res.set(corsHeaders);
  res.status(status).json(body);
}

function allowedEmail(email) {
  const list = AI_ALLOWED_EMAILS.value()
    .split(",")
    .map(x => x.trim().toLowerCase())
    .filter(Boolean);

  return list.length > 0 && list.includes(String(email || "").toLowerCase());
}

exports.atomicAi = onRequest(
  {
    region: "us-central1",
    secrets: [OPENAI_API_KEY],
    timeoutSeconds: 120,
    memory: "256MiB"
  },
  async (req, res) => {
    if (req.method === "OPTIONS") {
      res.set(corsHeaders);
      return res.status(204).send("");
    }

    if (req.method !== "POST") {
      return send(res, 405, { error: "POST required" });
    }

    try {
      const authHeader = req.get("Authorization") || "";
      if (!authHeader.startsWith("Bearer ")) {
        return send(res, 401, { error: "Authentication required" });
      }

      const idToken = authHeader.slice(7);
      const decoded = await admin.auth().verifyIdToken(idToken);

      if (!decoded.email || !allowedEmail(decoded.email)) {
        return send(res, 403, { error: "This Firebase account is not allowed to use Atomic AI." });
      }

      const { action, content, title, section, instruction } = req.body || {};
      const source = String(content || "").trim();

      if (!action && !instruction) {
        return send(res, 400, { error: "Missing AI action or instruction." });
      }

      const task = String(instruction || action);
      const context = [
        title ? "Article title: " + title : "",
        section ? "Section: " + section : "",
        source ? "Current article/editor text:\n" + source : ""
      ].filter(Boolean).join("\n\n");

      const system = [
        "You are Atomic Weekly's editorial AI assistant.",
        "Write clear, factual, publication-ready prose.",
        "Do not invent sources, quotes, statistics, names, or facts.",
        "When improving existing text, preserve its meaning unless the user explicitly asks for a change.",
        "Return only the requested result. Do not add meta commentary."
      ].join(" ");

      const prompt = context
        ? task + "\n\n" + context
        : task;

      const response = await fetch("https://api.openai.com/v1/responses", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": "Bearer " + OPENAI_API_KEY.value()
        },
        body: JSON.stringify({
          model: MODEL,
          instructions: system,
          input: prompt,
          store: false
        })
      });

      const data = await response.json();

      if (!response.ok) {
        console.error("OpenAI error:", data);
        return send(res, response.status, {
          error: data?.error?.message || "OpenAI request failed."
        });
      }

      return send(res, 200, {
        text: data.output_text || "",
        responseId: data.id || null,
        model: data.model || MODEL
      });
    } catch (error) {
      console.error("Atomic AI error:", error);
      return send(res, 500, { error: "AI service failed. Check Firebase Functions logs." });
    }
  }
);


function safeAssetName(name) {
  const raw = String(name || "image").trim();
  const extMatch = raw.match(/\.(png|jpe?g|webp|gif|svg)$/i);
  const ext = extMatch ? extMatch[0].toLowerCase() : "";
  const base = raw.replace(/\.[^.]+$/, "").replace(/[^a-zA-Z0-9_-]+/g, "-").replace(/^-+|-+$/g, "").slice(0, 80) || "image";
  return `atomic-${Date.now()}-${Math.random().toString(36).slice(2, 8)}-${base}${ext}`;
}

exports.atomicUploadAsset = onRequest(
  {
    region: "us-central1",
    secrets: [GITHUB_TOKEN],
    timeoutSeconds: 120,
    memory: "512MiB"
  },
  async (req, res) => {
    if (req.method === "OPTIONS") {
      res.set(corsHeaders);
      return res.status(204).send("");
    }
    if (req.method !== "POST") {
      return send(res, 405, { error: "POST required" });
    }

    try {
      const authHeader = req.get("Authorization") || "";
      if (!authHeader.startsWith("Bearer ")) {
        return send(res, 401, { error: "Authentication required" });
      }

      const decoded = await admin.auth().verifyIdToken(authHeader.slice(7));
      if (!decoded.email || !allowedEmail(decoded.email)) {
        return send(res, 403, { error: "This Firebase account is not allowed to upload assets." });
      }

      const { filename, mimeType, data } = req.body || {};
      const allowedTypes = new Set(["image/png", "image/jpeg", "image/webp", "image/gif", "image/svg+xml"]);
      if (!allowedTypes.has(String(mimeType || ""))) {
        return send(res, 400, { error: "Unsupported image type." });
      }
      if (typeof data !== "string" || !data) {
        return send(res, 400, { error: "Missing image data." });
      }

      const approxBytes = Math.floor(data.length * 3 / 4);
      if (approxBytes > 8 * 1024 * 1024) {
        return send(res, 413, { error: "Image must be 8 MB or smaller." });
      }

      const assetName = safeAssetName(filename);
      const path = `assets/${assetName}`;
      const apiUrl = "https://api.github.com/repos/iammdtanvirrahman/atomic-weekly/contents/" +
        encodeURIComponent(path).replace("%2F", "/");

      const gh = await fetch(apiUrl, {
        method: "PUT",
        headers: {
          "Accept": "application/vnd.github+json",
          "Authorization": "Bearer " + GITHUB_TOKEN.value(),
          "X-GitHub-Api-Version": "2022-11-28",
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          message: "Upload magazine asset: " + assetName,
          content: data,
          branch: "main"
        })
      });

      const result = await gh.json().catch(() => ({}));
      if (!gh.ok) {
        console.error("GitHub upload error:", result);
        return send(res, gh.status, {
          error: result?.message || "GitHub asset upload failed."
        });
      }

      const url = `https://iammdtanvirrahman.github.io/atomic-weekly/assets/${encodeURIComponent(assetName)}`;
      return send(res, 200, {
        ok: true,
        filename: assetName,
        path,
        url,
        githubUrl: result?.content?.html_url || null
      });
    } catch (error) {
      console.error("Atomic asset upload error:", error);
      return send(res, 500, { error: "Asset upload failed. Check Firebase Functions logs." });
    }
  }
);
