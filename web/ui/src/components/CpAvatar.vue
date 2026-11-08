<script setup lang="ts">
// A person as a round placeholder with their initials; there are no profile pictures yet.
withDefaults(defineProps<{ initials: string; tone?: 'primary' | 'secondary'; size?: 'sm' | 'md' }>(), { tone: 'secondary', size: 'sm' })

const tones = { primary: 'bg-primary text-primary-content', secondary: 'bg-secondary text-secondary-content' }
const sizes = { sm: 'w-8 text-xs', md: 'w-10' }
</script>

<template>
  <div class="cp-avatar cp-avatar-placeholder" data-component="avatar" aria-hidden="true">
    <div class="rounded-full" :class="[tones[tone], sizes[size]]">
      <span>{{ initials }}</span>
    </div>
  </div>
</template>
