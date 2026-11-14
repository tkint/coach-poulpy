import { ChevronRight, Contrast, Download, Gauge, Info, Mail, MoveVertical, UserRound, Weight } from '@lucide/vue'

/** The Lucide icons the mobile components draw by name, keyed by their Lucide name (ADR 0024). */
export const mobileIcons = {
  'chevron-right': ChevronRight,
  contrast: Contrast,
  download: Download,
  gauge: Gauge,
  info: Info,
  mail: Mail,
  'move-vertical': MoveVertical,
  weight: Weight,
  'user-round': UserRound,
}

export type MobileIconName = keyof typeof mobileIcons
