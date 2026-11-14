// Autosize the step textareas in the plain-HTML admin mockups, matching CpTextarea.vue.
document.querySelectorAll('textarea[data-autoresize]').forEach((field) => {
  const resize = () => {
    field.style.height = 'auto';
    const borderHeight = field.offsetHeight - field.clientHeight;
    field.style.height = field.scrollHeight + borderHeight + 'px';
  };

  resize();
  field.addEventListener('input', resize);

  if (typeof ResizeObserver !== 'undefined') {
    let width = field.getBoundingClientRect().width;
    const observer = new ResizeObserver(() => {
      const nextWidth = field.getBoundingClientRect().width;
      if (nextWidth !== width) {
        width = nextWidth;
        resize();
      }
    });
    observer.observe(field);
  } else {
    window.addEventListener('resize', resize);
  }
});
