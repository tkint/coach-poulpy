<script setup lang="ts">
// A person as a round placeholder with their initials; there are no profile pictures yet.
withDefaults(defineProps<{ initials: string; tone?: 'primary' | 'secondary'; size?: 'sm' | 'md' | 'lg' }>(), { tone: 'secondary', size: 'sm' })

const tones = { primary: 'bg-primary text-primary-content', secondary: 'bg-secondary text-secondary-content' }
const sizes = { sm: 'w-8 text-xs', md: 'w-10', lg: 'w-16 text-2xl' }
</script>

<template>
  <div class="cp-avatar cp-avatar-placeholder" data-component="avatar" aria-hidden="true">
    <div class="rounded-full" :class="[tones[tone], sizes[size]]">
      <span>{{ initials }}</span>
    </div>
  </div>
</template>
