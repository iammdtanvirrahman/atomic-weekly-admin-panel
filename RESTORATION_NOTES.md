# Atomic Weekly 3.0 — restored 2.0 capabilities

The `editorial-3.0` branch keeps the redesigned dashboard and writing studio as the default experience, while preserving the previous 2.0 workspaces and Firebase Functions.

## Entry points

- `index.html`: new 3.0 dashboard; choose **Classic control room** in the sidebar for the 2.0 dashboard.
- `editor.html`: new 3.0 writing studio; choose **Advanced tools** for the 2.0 editor.
- `classic-dashboard.html`: 2.0 dashboard, including the sign-in flow and existing management tools.
- `advanced-editor.html`: 2.0 editor, including AI actions, preview sizes, undo/redo, media gallery, asset upload, and scheduled publishing UI.
- `functions/index.js`: restored Firebase Functions endpoints `atomicAi`, `atomicUploadAsset`, and scheduled publisher `publishScheduledArticles`.

## Deploying the backend

The source files being present in GitHub does not deploy or activate Firebase Functions by itself. From a trusted local checkout, install Firebase CLI and deploy to the existing `atomictanvir` Firebase project after verifying the project and secrets:

1. Confirm the Firebase project is `atomictanvir` and the deployment account has permission.
2. Configure the Functions secrets `OPENAI_API_KEY` and `GITHUB_TOKEN`.
3. Set the `AI_ALLOWED_EMAILS` Functions parameter to the exact editor account email(s) allowed to use AI and upload assets.
4. Deploy with `firebase deploy --only functions --project atomictanvir`.
5. Verify the AI endpoint, upload endpoint, and scheduled publisher in Firebase logs before relying on them.

The scheduled publisher checks `atomicWeekly/scheduledArticles` every five minutes in the Asia/Dhaka timezone and publishes due entries to `publishedArticles`. The 2.0 editor's legacy draft workflow uses `atomicWeekly/drafts`; the 3.0 editor uses `draftArticles`. These are intentionally kept separate for compatibility, so drafts are not automatically migrated between editors.

## Verification status

GitHub source files and navigation links were re-fetched after committing. Live Firebase authentication, API secrets, actual image upload, scheduled publication, and browser interaction still need to be tested in the deployed Firebase project. Do not treat source presence alone as proof that backend deployment succeeded.
