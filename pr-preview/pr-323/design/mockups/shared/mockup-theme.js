// Light or dark for every mockup at once, apart from the page around them. The mockup pages on
// the docs site store the choice under this key; with no choice, the mockup follows the system
// setting through prefers-color-scheme in the token CSS. Open mockups follow a change made in
// another frame or tab.
const key = 'coach-poulpy-mockup-theme'

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
