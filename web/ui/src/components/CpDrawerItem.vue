<script setup lang="ts">
// One entry of the CpDrawer's sidebar: a Lucide icon (size-5) in the `icon` slot and a label that
// hides when the sidebar is folded to icons, where a tooltip names it instead. With `href` the entry
// is a link to another page; without it, a button.
defineProps<{ label: string; active?: boolean; href?: string }>()
</script>

<template>
  <li data-component="drawer-item">
    <component
      :is="href ? 'a' : 'button'"
      :href="href"
      :type="href ? undefined : 'button'"
      :class="[{ 'cp-menu-active': active }, 'cp-is-drawer-close:cp-tooltip cp-is-drawer-close:cp-tooltip-right']"
      :data-tip="label"
      :aria-current="active ? 'page' : undefined"
    >
      <slot name="icon" />
      <span class="cp-is-drawer-close:hidden">{{ label }}</span>
    </component>
  </li>
</template>
