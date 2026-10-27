// Light or dark for every mockup at once. The docs site's theme switch stores its choice under
// this key; with no choice ("auto" or nothing), the mockup follows the system setting through
// prefers-color-scheme in the token CSS. Open mockups follow a change made in another frame or tab.
const key = 'vitepress-theme-appearance'

function apply() {
  let choice = null
  try {
    choice = localStorage.getItem(key)
  } catch {}
  if (choice === 'dark' || choice === 'light') document.documentElement.dataset.theme = choice
  else delete document.documentElement.dataset.theme
}

apply()
addEventListener('storage', (event) => {
  if (event.key === key || event.key === null) apply()
})
