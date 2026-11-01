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
  'AGENTS.md': 'a link to CLAUDE.md, which is published',
  '.github/pull_request_template.md': 'a form GitHub fills in, not a guideline',
}
