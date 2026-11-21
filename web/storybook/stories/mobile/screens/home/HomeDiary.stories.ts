import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from '../screen'
import screenHomeDiary from './home-diary.html?raw'
import screenHomeDiaryNothingScheduled from './home-diary-nothing-scheduled.html?raw'
import screenHomeCalendarSheet from './home-calendar-sheet.html?raw'
import screenRecord from './record.html?raw'

// Home is the diary (what is next, then the past) on the compact top bar and the three-tab bar: Home · Library · You.
// Live becomes the Record button in its bar, which opens a bottom sheet.
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
/** Home · the calendar button on the Past heading opens the platform's own date picker, as everywhere else the app asks for a date: pick a day, confirm, and the timeline scrolls to that date. It does not mark the days with sessions; History's calendar does. */
export const HomeCalendarSheet: Story = { name: 'Home, jump to a day', ...screen(screenHomeCalendarSheet) }
/** Record · the Record button opens a bottom sheet over Home: a free session, a walk or a run, or a workout from the library. */
export const Record: Story = { name: 'Record', ...screen(screenRecord) }
