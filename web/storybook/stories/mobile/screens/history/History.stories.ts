import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from '../screen'
import screenHistoryFilterDuration from './history-filter-duration.html?raw'
import screenHistoryFilterZones from './history-filter-zones.html?raw'
import screenHistoryListMonth from './history-list-month.html?raw'
import screenHistoryListSearch from './history-list-search.html?raw'
import screenHistoryListWeek from './history-list-week.html?raw'
import screenHistorySessionDelete from './history-session-delete.html?raw'
import screenHistorySessionShare from './history-session-share.html?raw'

// The History screens of the apps (ADR 0029), drawn with the phone stylesheet (../screens.css).
// CLEANUP: the screens marked a11y todo carry axe findings the old mockups had (contrast of hints and past days, names of fields); fix them with the screens, then drop the todo.
const meta = {
  title: "Mobile/Screens/History",
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: {}, docs: { canvas: { sourceState: 'none' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** History · delete a session. */
export const HistorySessionDelete: Story = {
  name: "Delete a session",
  ...screen(screenHistorySessionDelete),
  parameters: { mockup: { issue: 104 } },
}
/** History · share a session. */
export const HistorySessionShare: Story = {
  name: "Share a session",
  ...screen(screenHistorySessionShare),
  parameters: { mockup: { issue: 104 } },
}

// History is list first (#511): a one-row week that opens into a floating month, then search and filters. Stats lives under You.
/** History · list first, the calendar collapsed to the week. Tapping the strip opens the month; swiping it changes week. The focused day is ringed in the calendar and the list scrolls to its sessions, which keep their normal look. */
export const HistoryListWeek: Story = {
  name: "List first, week",
  ...screen(screenHistoryListWeek),
  parameters: { mockup: { issue: 511 } },
}
/** History · the month opened from the week strip. Tapping a day focuses it and closes the month; swiping changes month. */
export const HistoryListMonth: Story = {
  name: "List first, month open",
  ...screen(screenHistoryListMonth),
  parameters: { mockup: { issue: 511 } },
}
/** History · searching by session or exercise name, with an equipment and a zones filter on. The calendar steps aside while the list is filtered. */
export const HistoryListSearch: Story = {
  name: "Search and filters",
  ...screen(screenHistoryListSearch),
  parameters: { mockup: { issue: 511 } },
}
/** History · the Zones filter, the library's body map. Equipment and Focus open the same sheets as the library's. */
export const HistoryFilterZones: Story = {
  name: "Filter by zones",
  ...screen(screenHistoryFilterZones),
  parameters: { mockup: { issue: 511 } },
}
/** History · the Duration filter, ranges to tick. Distance is the same sheet, in kilometres. */
export const HistoryFilterDuration: Story = {
  name: "Filter by duration",
  ...screen(screenHistoryFilterDuration),
  parameters: { mockup: { issue: 511 } },
}
