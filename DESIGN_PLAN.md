# Atomic Weekly Admin Panel — Editorial Studio Redesign Plan

**Target branch:** `editorial-3.0`  
**Scope:** New design specification. Implementation begins only after this plan is reviewed.  
**Product direction:** A premium, mobile-first editorial studio for publishing a science and culture magazine.

## 1. Vision and principles

Replace the conventional admin-dashboard experience with a focused publishing workspace: calm, precise, cinematic, and fast on Android and desktop. Writing is the primary task; controls should support it rather than compete with it.

- Editorial magazine identity rather than generic SaaS styling.
- Clear draft, save, preview, publish, and scheduled states.
- Responsive from narrow Android screens to wide desktop displays.
- Preserve working Firebase, authentication, publishing, draft, upload, preview, and PDF workflows.
- Do not change data contracts or remove features without auditing the current code and documenting the migration.

## 2. Visual direction

A premium science-magazine identity: deep black surfaces, restrained crimson and warm copper accents, and a warm paper-like writing canvas. Use cosmic artwork sparingly. Functional controls must remain readable and fast.

### Design tokens

| Token | Value | Purpose |
|---|---|---|
| App background | `#080809` | Main shell |
| Elevated surface | `#111114` | Panels and navigation |
| Raised surface | `#18181C` | Inputs and menus |
| Primary text | `#F5F5F7` | Main UI text |
| Secondary text | `#A1A1AA` | Supporting text |
| Crimson accent | `#C83D4D` | Primary action and active state |
| Copper accent | `#C58D69` | Editorial details |
| Writing paper | `#F4F0E8` | Paper mode |
| Writing ink | `#202027` | Text on paper |
| Success | `#4FA783` | Confirmed success |
| Warning | `#D6A04D` | Pending or warning |
| Error | `#E06464` | Actionable failure |

Use CSS custom properties and a centralized theme system. Avoid scattered hard-coded colors. Use an editorial display/serif face for headlines and a readable system sans-serif for controls. Maintain comfortable line lengths and contrast in dark and paper modes. Respect `prefers-reduced-motion`.

## 3. Information architecture

1. **Overview** — real counts, recent activity, pending submissions, quick actions.
2. **Articles** — searchable/filterable article library and status-aware actions.
3. **Writing Studio** — distraction-free long-form editor.
4. **Media Library** — image previews, paths, uploads, file details, and rotation controls.
5. **PDF / Magazine Library** — PDF metadata, cover selection, preview, and reader workflow.
6. **Submissions** — contribution review and status.
7. **Settings** — account and integration controls already supported by the application.

Do not invent metrics or expose navigation to unimplemented features.

## 4. Writing Studio

### Desktop
- Slim sticky toolbar: back, document status, preview, save state, publish.
- Large central writing canvas for title, summary/deck, body, and existing formatting controls.
- Secondary metadata panel for category, byline, tags, cover, and publication settings.
- Contextual media panel with image thumbnails and asset details.
- Side panels can collapse to maximize writing space.

### Mobile
- One-column, full-width writing canvas.
- Metadata and media open as separate dismissible drawers.
- Keep save/publish actions reachable without covering text input.
- Drawers must work with the existing JS state classes and include close controls.
- Test keyboard focus and scrolling while the virtual keyboard is open.

### Editor behavior
- Explicit unsaved, saving, saved, and error states.
- Autosave and draft recovery only through the existing persistence layer; prevent duplicate writes.
- Preview should closely match public article rendering.
- Warn before leaving with unsaved changes.
- Do not replace the editor engine or data format without a compatibility audit.

## 5. Metadata and AI assistance

Support title, summary/deck, category, author/byline, tags, cover image, and publication settings. Keep existing categories such as Science, Space, Physics, and Love where the content model supports them. Use inline validation and visible error text.

AI title/summary suggestions are optional and only enabled if the existing integration is configured. Suggestions must remain editable and require human review.

## 6. Media Library and asset manager

- Grid/list view, search, and file-type filters.
- Thumbnail preview beside every asset path or filename.
- Copyable paths, useful file details, upload progress, retryable errors.
- Rotate incorrectly oriented images with a clear before/after preview.
- Desktop drag-and-drop plus mobile file picker.
- Preserve existing GitHub-backed asset URLs; do not rewrite paths without a migration plan.
- Avoid oversized image previews and heavy DOM work that can freeze Android devices.

## 7. PDF / Magazine workflow

- Show each PDF with title, writer/byline, admin-selected cover, and status.
- Let the administrator choose/upload the cover and confirm its filename/path.
- The admin-provided cover takes priority; do not automatically publish a background extracted from the PDF.
- Preview/open the PDF in the existing reader before publishing.
- Publishing is an explicit Admin Panel action. Files appearing in `content/` must not automatically become public magazine entries.
- Preserve the existing PDF reader integration and data schema unless an intentional migration is documented.

## 8. Article library, submissions, and publishing

- Search and filter by category/status.
- Textual status labels as well as color; useful actions should reflect current capabilities.
- Review submissions with clear next steps.
- Publishing flow: validate fields → save draft → preview → publish now or schedule if supported → confirm backend result.
- Disable duplicate submission while a publish request is running.
- Never show success before the backend confirms it.
- Confirm destructive actions and make recovery possible when supported.

## 9. Functional constraints — must preserve

Audit and map current code paths before implementation. Preserve:
- Firebase configuration, authentication, database reads/writes, and access control.
- Article create/edit/save/draft/publish workflow and public-site data schema.
- Existing preview and rendering behavior.
- Asset upload and GitHub-backed storage paths.
- PDF metadata, cover selection, and reader integration.
- Existing categories, author/byline fields, and submissions.
- Scheduling or AI functionality only where a working integration already exists.
- Existing DOM IDs, handlers, routes, and payloads until replacements are deliberately migrated and tested.

Never commit GitHub tokens, private Firebase credentials, service-account keys, or other secrets.

## 10. Responsive and accessibility requirements

- Wide desktop: three-zone studio with collapsible side panels.
- Tablet: canvas stays primary; panels become toggles or overlays.
- Mobile: single-column canvas and full-screen/bottom-sheet drawers.
- Prevent horizontal overflow; use `min-width: 0` on flex/grid children.
- Avoid ambiguous nested scrolling and sticky elements that cover the editor.
- Semantic landmarks, logical headings, keyboard-accessible dialogs/drawers, visible focus, labels for icon-only buttons, meaningful image alt text, and sufficient contrast.
- Provide loading, empty, offline, expired-session, upload-failure, and publish-failure states.
- Clean up Firebase listeners/subscriptions to prevent duplicate listeners and leaks.

## 11. Suggested implementation phases

### Phase 0 — Audit
Inventory entry points, dependencies, Firebase operations, publishing endpoints, asset paths, editor hooks, PDF flow, and deployment configuration. Record baseline behavior and current errors.

### Phase 1 — Design foundation
Create tokens, typography, spacing, buttons, fields, status chips, dialogs, and shared responsive navigation. Verify dark and paper themes.

### Phase 2 — Studio shell
Implement the writing-first layout, sticky toolbar, metadata panel, and responsive media/metadata drawers. Connect to existing state.

### Phase 3 — Content workflows
Integrate preview, save/draft states, asset preview/upload, publish confirmation, and the supported PDF cover/reader flow.

### Phase 4 — Overview and libraries
Build real counts, article filtering, recent activity, and submission states with loading/empty/error handling.

### Phase 5 — QA and deployment
Test desktop and Android flows, inspect console errors, validate URLs and data contracts, then verify GitHub Pages source or Actions deployment and confirm the live URL.

## 12. Acceptance checklist

- [ ] This branch contains only this plan until implementation starts.
- [ ] Visual language is consistent across overview, studio, media, and PDF workflows.
- [ ] Writing canvas remains primary on desktop and mobile.
- [ ] Mobile drawers open/close reliably; no horizontal overflow.
- [ ] Dark and paper themes are both legible.
- [ ] Firebase authentication and data operations still work.
- [ ] Draft/save and publish states reflect actual backend responses.
- [ ] Preview matches public article rendering.
- [ ] Asset preview, path, upload errors, and rotation work where supported.
- [ ] PDF title, author, admin-selected cover, reader preview, and explicit publish flow work.
- [ ] No duplicate Firebase listeners or new uncaught console errors.
- [ ] Deployment source and live result are verified.
- [ ] No secrets are committed.

## Definition of done

A polished visual redesign is not complete if authentication, assets, drafts, PDFs, or publishing break. The target is a coherent premium editorial studio with a reliable real publishing workflow.

**Implementation rule:** Audit before changing behavior, deliver in phases, and update this plan when a deliberate product decision changes.
