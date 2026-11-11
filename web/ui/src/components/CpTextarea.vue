<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, useId, watch, useTemplateRef } from 'vue'

// A multiline field that grows with its content. The model stays the source of truth; resizing
// also runs after external model changes and when the available width changes and text re-wraps.
const props = withDefaults(
  defineProps<{
    label: string
    hint?: string
    placeholder?: string
    rows?: number
    invalid?: boolean
    disabled?: boolean
    required?: boolean
    autocomplete?: string
  }>(),
  { rows: 2 },
)
const value = defineModel<string>({ default: '' })
const id = useId()
const field = useTemplateRef<HTMLTextAreaElement>('field')
let observer: ResizeObserver | undefined
let previousWidth = 0

async function resize() {
  await nextTick()
  const element = field.value
  if (!element) return
  element.style.height = 'auto'
  const borderHeight = element.offsetHeight - element.clientHeight
  element.style.height = `${element.scrollHeight + borderHeight}px`
}

watch(value, resize, { flush: 'post' })
watch(() => props.rows, resize)

onMounted(() => {
  void resize()
  if (typeof ResizeObserver !== 'undefined' && field.value) {
    observer = new ResizeObserver(([entry]) => {
      const width = entry.contentRect.width
      if (width !== previousWidth) {
        previousWidth = width
        void resize()
      }
    })
    observer.observe(field.value)
  }
})

onBeforeUnmount(() => observer?.disconnect())
</script>

<template>
  <div class="cp-fieldset" data-component="textarea-field">
    <label class="cp-type-label" :for="id">{{ label }}</label>
    <textarea
      :id="id"
      ref="field"
      v-model="value"
      data-autoresize
      class="cp-textarea w-full"
      :rows="rows"
      :placeholder="placeholder"
      :disabled="disabled"
      :required="required"
      :autocomplete="autocomplete"
      :aria-invalid="invalid ? 'true' : undefined"
      :aria-describedby="hint ? `${id}-hint` : undefined"
    />
    <p v-if="hint" :id="`${id}-hint`" class="cp-label whitespace-normal">{{ hint }}</p>
  </div>
</template>

<style scoped>
textarea.cp-textarea {
  display: block;
  width: 100%;
  min-height: 4.5rem;
  padding: var(--cp-space-md);
  border: 1.5px solid var(--cp-outline);
  border-radius: var(--cp-radius-md);
  background: var(--cp-surface-variant);
  color: var(--cp-on-surface);
  font: inherit;
  line-height: 1.5;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  resize: none;
  overflow-y: hidden;
}
textarea.cp-textarea:focus-visible {
  outline: 2px solid var(--cp-primary);
  outline-offset: 2px;
}
</style>
