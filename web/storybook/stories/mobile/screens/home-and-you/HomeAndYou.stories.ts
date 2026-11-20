import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from '../screen'
import screenAchievements from './achievements.html?raw'
import screenHomeDiary from './home-diary.html?raw'
import screenHomeDiaryNothingScheduled from './home-diary-nothing-scheduled.html?raw'
import screenSettings from './settings.html?raw'
import screenProfile from './profile.html?raw'
import screenProfileSignedIn from './profile-signed-in.html?raw'
import screenRecord from './record.html?raw'
import screenYouEditStatistics from './you-edit-statistics.html?raw'
import screenYouGuest from './you-guest.html?raw'
import screenYouPending from './you-pending.html?raw'
import screenYouSignedIn from './you-signed-in.html?raw'

// The new Home, You and profile screens, on the compact top bar and the three-destination bar:
// Home · Library · You. Home is the diary (what is next, then the past), Live becomes the Record button in its bar,
// Statistics sit on You under the profile card, and Settings sits behind the cog. Nothing is built until a
// PR agrees them.
// CLEANUP: these screens draw six Lucide icons the apps don't have yet (award, circle-plus, circle-question-mark, key-round, ruler, smartphone). Run `node design/build-icons.mjs --add award circle-plus circle-question-mark key-round ruler smartphone` when the screens are agreed; it needs network access to jsdelivr, which this session's sandbox blocked. They replace the History, You and Settings drafts of #511 and #512 where they overlap.
const meta = {
  title: 'Mobile/Screens/Home and You',
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: {}, docs: { canvas: { sourceState: 'none' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Home · the diary: a slim motivation row, what is next as a tappable card, then the past with a recap and a record between sessions. Record sits in the top bar. */
export const HomeDiary: Story = { name: 'Home, the diary', ...screen(screenHomeDiary) }
/** Home · nothing planned: Up next suggests a short workout, as a tappable card. */
export const HomeDiaryNothingScheduled: Story = { name: 'Home, nothing scheduled', ...screen(screenHomeDiaryNothingScheduled) }
/** Record · the Record button opens a bottom sheet over Home: a free session, a walk or a run, or a workout from the library. */
export const Record: Story = { name: 'Record', ...screen(screenRecord) }
/** Achievements · opened from the motivation row: Poulpy and the eight tentacles, what is earned, and what is still hidden. */
export const Achievements: Story = { name: 'Achievements', ...screen(screenAchievements) }
/** You · a guest: the profile card carries the Guest badge; Statistics follow it, with a period switch (7 days, 30 days, Year, All time) and an Edit button; the cog opens Settings. */
export const YouGuest: Story = { name: 'You, guest', ...screen(screenYouGuest) }
/** You · account created, email not confirmed yet. */
export const YouPending: Story = { name: 'You, confirm your email', ...screen(screenYouPending) }
/** You · signed in. */
export const YouSignedIn: Story = { name: 'You, signed in', ...screen(screenYouSignedIn) }
/** Edit statistics · a switch and a handle per block, to choose and order what You shows. */
export const YouEditStatistics: Story = { name: 'Edit statistics', ...screen(screenYouEditStatistics) }
/** Settings · Appearance, Region (language and units) and Devices, all on the page, reached from the cog on You. */
export const Settings: Story = { name: 'Settings', ...screen(screenSettings) }
/** Profile · a guest: one Account row on top (it opens Create an account and Sign in), then about you with a Poulpy face, training and body. */
export const Profile: Story = { name: 'Profile, guest', ...screen(screenProfile) }
/** Profile · signed in: one Account row at the top, showing the email; it opens the existing account screen (password, devices, export, sign out, delete). */
export const ProfileSignedIn: Story = { name: 'Profile, signed in', ...screen(screenProfileSignedIn) }
