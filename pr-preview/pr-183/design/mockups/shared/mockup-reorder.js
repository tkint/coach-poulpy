/* Demo only: the real apps animate the move natively. */
(function () {
  var lists = document.querySelectorAll('[data-reorder]');
  function sync(list) {
    var items = Array.prototype.filter.call(list.children, function (c) { return c.tagName === 'LI'; });
    items.forEach(function (li, i) {
      var group = li.querySelector(':scope > .move-buttons, :scope > div > .move-buttons');
      if (!group) return;
      group.querySelector('[data-move=up]').disabled = i === 0;
      group.querySelector('[data-move=down]').disabled = i === items.length - 1;
    });
  }
  lists.forEach(sync);
  document.addEventListener('click', function (e) {
    var b = e.target.closest('[data-move]');
    if (!b || b.disabled) return;
    var li = b.closest('li'), list = li.parentElement;
    var other = b.dataset.move === 'up' ? li.previousElementSibling : li.nextElementSibling;
    if (!other) return;
    var a = li.getBoundingClientRect(), o = other.getBoundingClientRect();
    if (b.dataset.move === 'up') list.insertBefore(li, other); else list.insertBefore(other, li);
    var a2 = li.getBoundingClientRect(), o2 = other.getBoundingClientRect();
    var reduce = matchMedia('(prefers-reduced-motion: reduce)').matches;
    if (!reduce) {
      li.animate([{ transform: 'translateY(' + (a.top - a2.top) + 'px)' }, { transform: 'none' }], { duration: 250, easing: 'ease-out' });
      other.animate([{ transform: 'translateY(' + (o.top - o2.top) + 'px)' }, { transform: 'none' }], { duration: 250, easing: 'ease-out' });
    }
    li.classList.add('just-moved'); setTimeout(function () { li.classList.remove('just-moved'); }, 700);
    sync(list);
    var again = li.querySelector('[data-move=' + b.dataset.move + ']');
    (again && !again.disabled ? again : li.querySelector('[data-move]:not(:disabled)')).focus({ preventScroll: true });
    li.scrollIntoView({ block: 'nearest', behavior: reduce ? 'auto' : 'smooth' });
  });
})();
