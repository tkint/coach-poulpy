import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from '../screen'
import screenHomeDiary from './home-diary.html?raw'
import screenHomeDiaryNothingScheduled from './home-diary-nothing-scheduled.html?raw'
import screenHomeCalendarSheet from './home-calendar-sheet.html?raw'
import screenRecord from './record.html?raw'

// Home is the diary (what is next, then the past) on the compact top bar and the three-tab bar: Home · Library · You.
// Live becomes the Record button in its bar, which opens a bottom sheet.
// CLEANUP: these screens draw six Lucide icons the apps don't have yet (award, circle-plus, circle-question-mark, key-round, ruler, smartphone). Run `node design/build-icons.mjs --add award circle-plus circle-question-mark key-round ruler smartphone` when the screens are agreed; it needs network access to jsdelivr, which this session's sandbox blocked. They replace the History, You and Settings drafts of #511 and #512 where they overlap.
const meta = {
  title: 'Mobile/Screens/Home/Diary',
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: {}, docs: { canvas: { sourceState: 'none' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Home · the diary: a slim motivation row, what is next as a tappable card, then the past with a recap and a record between sessions. Record sits in the top bar. */
export const HomeDiary: Story = { name: 'Home, the diary', ...screen(screenHomeDiary) }
/** Home · nothing planned: Up next suggests a short workout, as a tappable card. */
export const HomeDiaryNothingScheduled: Story = { name: 'Home, nothing scheduled', ...screen(screenHomeDiaryNothingScheduled) }
/** Home · the Calendar button opens a bottom sheet that is only a shortcut: tap a day and it closes and the timeline scrolls to that date. */
export const HomeCalendarSheet: Story = { name: 'Home, jump to a day', ...screen(screenHomeCalendarSheet) }
/** Record · the Record button opens a bottom sheet over Home: a free session, a walk or a run, or a workout from the library. */
export const Record: Story = { name: 'Record', ...screen(screenRecord) }
