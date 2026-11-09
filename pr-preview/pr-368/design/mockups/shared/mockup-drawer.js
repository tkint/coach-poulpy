// Opens a page's side drawer on a large screen and closes it below, as the app will, since
// DaisyUI's drawer shows icons only until its toggle is checked. The docs site resizes the frame
// without reloading it, so this follows the width.
const large = matchMedia('(min-width: 1024px)')
const sync = () => {
  for (const toggle of document.querySelectorAll('.cp-drawer-toggle')) toggle.checked = large.matches
}
large.addEventListener('change', sync)
sync()
