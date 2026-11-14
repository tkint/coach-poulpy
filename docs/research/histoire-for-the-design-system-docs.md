# Histoire or Storybook as the home of the design system documentation

Research notes, 14 November 2026. Primary sources only: the projects' own documentation, source files, changelogs and the npm registry. Where I could not confirm something it says so. Nothing here has been run in this repo.

## Short answer

Histoire could technically hold most of it, but it is the weaker choice today, and the owner's goal (one place, with checks that fail CI) is already met by Storybook. Keep Storybook ([ADR 0028](../adr/0028-storybook-is-the-component-workshop-of-the-design-system.md)) and grow it into the whole design system home with MDX pages.

- Histoire's last published version is `1.0.0-beta.1`, from 7 January 2026, and its `latest` tag is still a beta. Vite 8 support, the browser test runner and the accessibility plugin exist only on its `main` branch, not in a published release (checked on npm on 14 November 2026).
- Histoire can make standalone Markdown pages, but they are static Markdown. Live component samples, swatches and iframes would have to be hand-built Vue stories. Storybook MDX mixes prose, live stories and ready-made colour and type blocks in one page.
- Both build to static files and both can sit under a sub-path. Storybook is already doing it on the docs site.

## Context

- `web/ui` is Vue 3.5, Tailwind 4.3.5 and DaisyUI 5.7.50, pinned exactly ([ADR 0023](../adr/0023-the-websites-share-one-vue-and-daisyui-design-system.md)). `web/storybook` pins Storybook 10.6.7, `@storybook/vue3-vite`, `@storybook/addon-a11y`, `@storybook/addon-docs`, `@storybook/addon-vitest` and Vitest 5.1.0 (`web/storybook/package.json`).
- The docs site is VitePress. Its top menu is `themeConfig.nav` in `site/.vitepress/config.mts`, and `site/copy-docs.mjs` builds Storybook and copies it to `public/storybook/`.
- The Design system section today holds written rules, token swatches, live component samples with classes, Android and iOS component descriptions, and plain HTML mockups in iframes (`site/.vitepress/config.mts`, sidebar key "Design system").

## 1. Version, cadence, maintenance, compatibility

| | Histoire | Storybook |
| --- | --- | --- |
| Latest on npm | `1.0.0-beta.1`, 7 January 2026 | `10.6.7`, 10 November 2026 |
| Recent releases | alpha.1 (22 Jan 2025), alpha.2 (21 Feb), alpha.3 (30 Jul), alpha.4 (20 Oct), alpha.5 (20 Nov), beta.1 (7 Jan 2026), nothing since | 10.5.9 (18 Aug 2026), 10.6.0 (2 Sep), 10.6.1 (29 Sep), 10.6.2 (12 Oct), 10.6.3 (20 Oct), 10.6.4 (26 Oct), 10.6.5 (27 Oct), 10.6.6 (5 Nov), 10.6.7 (10 Nov) |
| Vite | Published beta.1 peer range is `^7.3.0`. `main` declares `^7.0.0 \|\| ^8.0.0` | `@storybook/vue3-vite` 10.6.7 peers `^5 \|\| ^6 \|\| ^7 \|\| ^8` |
| Vue | `@histoire/plugin-vue` beta.1 peers `vue ^3.5.26`. The changelog says beta.1 fixes story collection on Vue 3.5.26 and later | Vue 3 through `@storybook/vue3-vite` |
| Tailwind 4 | No statement of support found. The changelog lists a "Tailwind design system story styling" fix in alpha.5, and `main` has a `test:tailwind:integration` script | Already works in this repo through `@tailwindcss/vite` in `viteFinal` |

What this means:

- The 0.17 line stopped on 9 April 2024, then 1.0 went through five alphas and one beta over about a year. There has been no published release for about ten months. I could not read the repo's commit dates or issue counts (GitHub API access was refused), so "maintenance status" is judged from the npm record only. The `main` branch has clearly moved on (new docs for visual testing, embedding, an MCP server, Vite 8), so the project is not abandoned, but its newest work is unreleased.
- The Histoire docs site describes `main`. Pages marked "1.0-beta.2+" describe unreleased features, and the docs say to try them with the project's continuous releases.
- Histoire requires Node 22.12 or later and says "Vite 7 or 8" on its getting-started page, where Vite 8 is marked as "1.0-beta.2+". The repo has Vite 8 in play (Storybook's Vite peer range allows it), so a published Histoire today would pin the repo to Vite 7 (inference from the peer range).

## 2. Standalone written pages and how rich they can be

Histoire:

- A file ending `.story.md` with no sibling story file becomes a virtual story that renders the Markdown as a page. Front matter sets `id`, `title`, `icon`, `iconColor` and `group`. Pages can link to stories and to each other by relative path.
- Markdown also attaches to a story as a `<docs lang="md">` block or a sibling `.md` file.
- The renderer is markdown-it with `html: true`, anchors, attributes and emoji, and the config has a `markdown` hook to extend it. Raw HTML in a page is therefore passed through. I did not test whether an `<iframe>` inside such a page renders in the workbench, and there is no documented way to put a Vue component inside Markdown.
- Live samples, swatches and the plain-HTML mockup iframes would be written as `.story.vue` files, with variants, not as prose pages. There is no ready-made colour or type block. The `tree` config can group and order pages, and the docs say stories can be placed by title path, so a "Design system" tree is possible.
- Tokens could be shown as a Vue story that reads `tokens.css` or `design/tokens.json`. This is the same work as a Storybook MDX page but without a swatch block (inference).

Storybook:

- An MDX file with `<Meta title="..." />` and no `of` prop is a documentation-only page. Without `Meta` it still appears as a Docs entry by file location.
- MDX embeds stories with `<Canvas of=... />` and `<Story of=... />`, renders an external `.md` with the `Markdown` block, and takes GitHub-style tables through `remark-gfm`.
- Doc blocks include `ColorPalette`, `ColorItem`, `Typeset`, `IconGallery`, `Canvas`, `Story` and `Source`. These cover the swatches and type specimens directly.
- Raw HTML and iframes are not covered on the MDX page, but MDX allows arbitrary JSX, so an `<iframe>` element should work (inference, not tested here). The MDX page's wording speaks of React components. The Storybook Vue docs were not checked for MDX specifics, so confirm in a spike that `<Canvas>` and `<Story>` render a Vue story in this repo.

Android and iOS descriptions are plain prose, so either tool handles them as Markdown. Neither can show a Compose or SwiftUI component live.

## 3. Interaction tests, accessibility, CI

| | Histoire | Storybook (already in the repo) |
| --- | --- | --- |
| Interaction tests | `onTest` inside a story with `describe`, `it`, `expect` from Vitest, run with `histoire test`. Needs `vitest` and `@vitest/browser-playwright` on the same version, plus Playwright. No snapshot assertions, no fake timers | `play` functions run by `@storybook/addon-vitest`: every story becomes a Vitest test in browser mode, and a play function's assertions are checked |
| Accessibility | `@histoire/plugin-a11y` on `main`: axe-core 4.14.0, an Accessibility inspector tab, `test: true` adds an axe test per variant, and `assertA11y(canvas)` checks interaction states | `@storybook/addon-a11y` on axe-core. `parameters.a11y.test` is `'error'`, `'todo'` or `'off'`, and `'error'` fails CI through the Vitest addon |
| Visual tests | Built in: a `visual` config block, `histoire visual` CLI with exit codes, baselines committed in `.histoire-visual/`, per-OS baselines | Chromatic (a cloud service), not used here |
| In CI | `histoire test` and `histoire visual` | `vitest run` (the repo's `npm test -w storybook`) |
| Published? | The plugin is not on npm (the registry returns "Not found" for `@histoire/plugin-a11y`), and the published `histoire@1.0.0-beta.1` declares no Vitest peers. So these features are `main` only | Published and in use |

The Histoire testing docs say the closest equivalent to "light and dark a11y runs" is not described; ADR 0028's "in light and dark" check would need a custom setup in either tool, and today it is only partly covered by the toolbar switch in Storybook (not verified whether both themes are tested in CI).

## 4. Static build, sub-path, light and dark

Histoire:

- `histoire build` is a static build, with output in `.histoire/dist` by default. A Node deployment target (`--target node`) is also new in beta.2+.
- The base path is set with `vite: { base: '/stories/' }`. Embed URLs are built from it, and the whole built output must be deployed together (embedding guide).
- The router has a `'hash'` mode, intended for static hosts without history fallback.
- The toolbar has a light/dark button. It adds the class `dark` (config `theme.darkClass`) to the story preview, with `defaultColorScheme` of `'light'`, `'dark'` or `'auto'`, `hideColorSchemeSwitch` and `storeColorScheme`. This repo switches on `data-theme` on `<html>` (`tokens.css`), so a setup file would have to translate (inference).

Storybook:

- `storybook build` gives a static app that "any web server" can serve. The publishing page does not discuss sub-paths. In this repo it is already built and copied to `site/public/storybook/` and shown at `/storybook/` (ADR 0028, `site/copy-docs.mjs`), so sub-path hosting is proven here, including with the docs site's `SITE_BASE`. Whether `SITE_BASE` is non-root in production, I did not check.
- The theme switch is a toolbar global plus a decorator, and `web/storybook/.storybook/preview.ts` already sets `data-theme`. The Storybook toolbar docs say nothing about the docs pages' theme, so MDX pages follow whatever the decorator and preview CSS do (not verified).

## 5. Reaching it from the docs site's top menu

Both tools publish a self-contained static app, so both are reached the same way.

- The top menu is `themeConfig.nav` in `site/.vitepress/config.mts`, now five items, one of them "Design system" pointing at `/docs/design-system`.
- VitePress nav `link` takes a full URL, or a path without `.md` that must start with `/`. It sets `target` and `rel` itself for external links and lets you override them. The nav page does not say how to link to a static folder under `public`, so test `link: '/storybook/'` (and `target: '_self'`) on a PR preview, with `/storybook/index.html` as the fallback (unverified).
- To make it the one home: add the MDX pages, replace the "Design system" nav item with a link to the built Storybook, drop the section's sidebar key, and keep `StoryFrame` embeds only where another page needs one.
- The cost is that readers leave the VitePress theme, search and sidebar when they click that menu item. The owner's wish for "one place" accepts this. A Storybook-side link back to the docs site (a `brandTitle` or a sidebar link) would soften it, but I did not check how Storybook 10 sets that.
- For Histoire the same plan applies, with `base` set to the sub-path and the output copied to `site/public/`.

## 6. Verdict for this repo

Stay on Storybook.

- It is released and moving every week or two. Histoire's useful new parts (Vite 8, `histoire test`, the a11y plugin) are not published.
- The repo already has Storybook's tests, axe check and theme switch working in CI, and ADR 0028 already weighed Histoire and chose against it for the same reason. Nothing found here changes that, though the ADR's line "updated less often" is now confirmed by the npm record and its "thinner testing" is out of date on `main` (but not in a release).
- Histoire's standalone Markdown pages are a good match for prose, but live samples, swatches and iframes would all be Vue stories, which Storybook's MDX and doc blocks do more directly.
- Revisit Histoire only if a stable 1.0 ships with Vite 8, `histoire test` and the a11y plugin, and Storybook becomes a burden.

Suggested next step if the owner agrees: a spike that adds three MDX pages to `web/storybook` (a written-rules page from `docs/design-system.md`, a tokens page with `ColorPalette`, and a mockups page with iframes), then checks the sub-path and the nav link on a PR preview. That would also answer the points marked unverified above.

## Not verified

- Histoire's commit and issue activity (GitHub API access refused). The cadence is from npm and the changelog only.
- Whether Histoire's `html: true` Markdown renders an `<iframe>` in the workbench.
- Whether MDX `<Canvas>`, `<Story>` and `<iframe>` work with `@storybook/vue3-vite` here.
- How a VitePress nav link to a folder in `public` behaves.
- Whether Histoire's Tailwind 4 support is official (no statement found).

## Sources

- Histoire npm record (versions and dates, peer ranges): https://registry.npmjs.org/histoire, https://registry.npmjs.org/histoire/1.0.0-beta.1, https://registry.npmjs.org/@histoire/plugin-vue/1.0.0-beta.1, https://registry.npmjs.org/@histoire/plugin-a11y
- Histoire changelog: https://raw.githubusercontent.com/histoire-dev/histoire/main/CHANGELOG.md
- Histoire releases: https://github.com/histoire-dev/histoire/releases
- Histoire `main` package manifest and engines: https://raw.githubusercontent.com/histoire-dev/histoire/main/package.json, https://raw.githubusercontent.com/histoire-dev/histoire/main/packages/histoire/package.json
- Histoire getting started: https://histoire.dev/guide/getting-started.html (source: https://raw.githubusercontent.com/histoire-dev/histoire/main/docs/guide/getting-started.md)
- Histoire documentation and standalone pages: https://histoire.dev/guide/vue3/docs (source: https://raw.githubusercontent.com/histoire-dev/histoire/main/docs/guide/vue3/docs.md)
- Histoire Markdown renderer: https://raw.githubusercontent.com/histoire-dev/histoire/main/packages/histoire/src/node/markdown/renderer.ts
- Histoire testing: https://histoire.dev/guide/testing
- Histoire visual testing: https://histoire.dev/guide/visual-testing
- Histoire accessibility plugin: https://raw.githubusercontent.com/histoire-dev/histoire/main/docs/guide/plugins/a11y.md, official plugin list https://raw.githubusercontent.com/histoire-dev/histoire/main/docs/guide/plugins/official.md
- Histoire embedding and base path: https://histoire.dev/guide/embedding
- Histoire configuration: https://histoire.dev/guide/config, https://histoire.dev/reference/config (source: https://raw.githubusercontent.com/histoire-dev/histoire/main/docs/reference/config.md)
- Histoire Node deployment: https://raw.githubusercontent.com/histoire-dev/histoire/main/docs/guide/deploy-node.md
- Storybook npm record: https://registry.npmjs.org/storybook, https://registry.npmjs.org/@storybook/vue3-vite
- Storybook MDX: https://storybook.js.org/docs/writing-docs/mdx
- Storybook doc blocks: https://storybook.js.org/docs/writing-docs/doc-blocks
- Storybook Vitest addon: https://storybook.js.org/docs/writing-tests/integrations/vitest-addon
- Storybook accessibility testing: https://storybook.js.org/docs/writing-tests/accessibility-testing
- Storybook publishing: https://storybook.js.org/docs/sharing/publish-storybook
- Storybook toolbars and globals: https://storybook.js.org/docs/essentials/toolbars-and-globals
- VitePress nav: https://vitepress.dev/reference/default-theme-nav
- In this repo: `docs/adr/0023-the-websites-share-one-vue-and-daisyui-design-system.md`, `docs/adr/0028-storybook-is-the-component-workshop-of-the-design-system.md`, `web/storybook/package.json`, `web/storybook/.storybook/main.ts`, `web/storybook/.storybook/preview.ts`, `site/.vitepress/config.mts`, `site/copy-docs.mjs`
