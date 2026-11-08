<script setup lang="ts">
// A table that scrolls sideways on a phone. Put a thead and a tbody in it.
</script>

<template>
  <div class="overflow-x-auto" data-component="table">
    <table class="cp-table"><slot /></table>
  </div>
</template>
