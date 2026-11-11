<script setup lang="ts">
// A table that scrolls sideways on a phone. Put a thead and a tbody in it.
</script>

<template>
  <div class="overflow-x-auto rounded-box border border-base-300 bg-base-100" data-component="table">
    <table class="cp-table"><slot /></table>
  </div>
</template>
