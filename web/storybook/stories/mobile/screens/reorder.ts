/** Demo only: the real apps animate the move natively. The up and down buttons of a `[data-reorder]` list move its rows. */
export function enableReorder(root: HTMLElement) {
  const sync = (list: Element) => {
    const items = Array.from(list.children).filter((child) => child.tagName === 'LI')
    items.forEach((li, at) => {
      const group = li.querySelector(':scope > .move-buttons, :scope > div > .move-buttons')
      if (!group) return
      group.querySelector<HTMLButtonElement>('[data-move=up]')!.disabled = at === 0
      group.querySelector<HTMLButtonElement>('[data-move=down]')!.disabled = at === items.length - 1
    })
  }
  root.querySelectorAll('[data-reorder]').forEach(sync)
  root.addEventListener('click', (event) => {
    const button = (event.target as Element).closest<HTMLButtonElement>('[data-move]')
    if (!button || button.disabled) return
    const li = button.closest('li')!
    const list = li.parentElement!
    const other = button.dataset.move === 'up' ? li.previousElementSibling : li.nextElementSibling
    if (!other) return
    const before = [li, other].map((row) => row.getBoundingClientRect())
    if (button.dataset.move === 'up') list.insertBefore(li, other)
    else list.insertBefore(other, li)
    const after = [li, other].map((row) => row.getBoundingClientRect())
    const reduce = matchMedia('(prefers-reduced-motion: reduce)').matches
    if (!reduce) {
      ;[li, other].forEach((row, at) => row.animate([{ transform: `translateY(${before[at].top - after[at].top}px)` }, { transform: 'none' }], { duration: 250, easing: 'ease-out' }))
    }
    li.classList.add('just-moved')
    setTimeout(() => li.classList.remove('just-moved'), 700)
    sync(list)
    const again = li.querySelector<HTMLButtonElement>(`[data-move=${button.dataset.move}]`)
    ;(again && !again.disabled ? again : li.querySelector<HTMLButtonElement>('[data-move]:not(:disabled)'))?.focus({ preventScroll: true })
    li.scrollIntoView({ block: 'nearest', behavior: reduce ? 'auto' : 'smooth' })
  })
}
