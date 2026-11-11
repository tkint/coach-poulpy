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
  '.agents/skills/area-guidance/SKILL.md': 'Codex skill entry point; the detailed area guidance is published in docs/agents/',
  '.agents/skills/code-graph/SKILL.md': 'Codex skill entry point; the detailed code-graph guide is published in docs/agents/',
  '.agents/skills/domain-context/SKILL.md': 'Codex skill entry point; the detailed domain guide is published in docs/agents/',
  '.agents/skills/implement/SKILL.md': 'Codex workflow entry point; shared project guidance is published in CLAUDE.md and docs/agents/',
  '.agents/skills/issue-tracker/SKILL.md': 'Codex skill entry point; the detailed issue-tracker guide is published in docs/agents/',
  '.agents/skills/refine-issue/SKILL.md': 'Codex skill entry point; the detailed issue-refinement guide is published in docs/agents/',
  '.agents/skills/triage-issues/SKILL.md': 'Codex skill entry point; the detailed triage guide is published in docs/agents/',
  '.github/pull_request_template.md': 'a form GitHub fills in, not a guideline',
  '.github/copilot-instructions.md': "the Copilot cloud agent's sandbox, not a guideline; CLAUDE.md holds the rules",
}
