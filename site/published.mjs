// What the docs site publishes from the rest of the repo, read by copy-docs.mjs and
// check-published.mjs. Every guideline is published: a new Markdown file outside site/ either goes
// in `shared` or in `unpublished` with the reason, or check-published.mjs fails CI.
export const shared = [
  'CONTEXT.md',
  'CONTRIBUTING.md',
  'CLAUDE.md',
  'docs',
  'design/README.md',
  'design/body-map-source/NOTICE.md',
  'i18n/README.md',
  'i18n/glossary',
]

export const unpublished = {
  'README.md': 'the GitHub front page; the site home and Contributing cover it',
  'AGENTS.md': 'Codex entry point; shared project guidance is published in CLAUDE.md and docs/agents/',
  'web/storybook/stories': "the classes each web component renders, shown on that component's docs page in the component workshop, which the docs site links from its top menu (ADR 0028)",
  '.agents/skills': 'agent skills are consumed by coding agents; only shared project guidance is published',
  '.github/pull_request_template.md': 'a form GitHub fills in, not a guideline',
  '.github/copilot-instructions.md': "the Copilot cloud agent's sandbox, not a guideline; CLAUDE.md holds the rules",
}
