import { icons } from '@lucide/vue'
import type { Component } from 'vue'

/** A Lucide icon by its Lucide name, such as `chevron-right` (ADR 0024). */
export const lucideIcon = (name: string): Component => {
  const icon = (icons as Record<string, Component>)[name.replace(/(^|-)(\w)/g, (_, __, c: string) => c.toUpperCase())]
  if (!icon) throw new Error(`No Lucide icon called "${name}"`)
  return icon
}
