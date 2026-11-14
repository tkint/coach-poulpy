<script setup lang="ts">
// A table that scrolls sideways on a phone. Put a thead and a tbody in it. It draws on an opaque surface with a
// border, so its rows read the same on a card, on the page and in a dark theme: the header is set apart from
// the rows, the rows from each other, and a hovered or selected row (`cp-hover`, `aria-selected`) is shaded.
</script>

<template>
  <div class="overflow-x-auto rounded-box border border-base-300 bg-base-100" data-component="table">
    <table class="cp-table bg-base-100"><slot /></table>
  </div>
</template>
