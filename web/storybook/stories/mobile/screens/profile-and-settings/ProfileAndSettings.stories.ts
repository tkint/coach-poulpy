import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from '../screen'
import screenBodyHeightEmpty from './body-height-empty.html?raw'
import screenBodyHeight from './body-height.html?raw'
import screenBodyLogHeightSheet from './body-log-height-sheet.html?raw'
import screenBodyLogSheet from './body-log-sheet.html?raw'
import screenBodyWeightEmpty from './body-weight-empty.html?raw'
import screenBodyWeight from './body-weight.html?raw'
import screenDevicesList from './devices-list.html?raw'
import screenProfileBodyFigureSheet from './profile-body-figure-sheet.html?raw'
import screenProfileEdit from './profile-edit.html?raw'
import screenProfileExperienceSheet from './profile-experience-sheet.html?raw'
import screenProfileGoalsSheet from './profile-goals-sheet.html?raw'
import screenProfilePainSheet from './profile-pain-sheet.html?raw'
import screenProfilePronounsSheet from './profile-pronouns-sheet.html?raw'
import screenProfileSexSheet from './profile-sex-sheet.html?raw'
import screenSettingsAppIconSheet from './settings-app-icon-sheet.html?raw'
import screenSettingsGeneral from './settings-general.html?raw'
import screenSettingsPreferences from './settings-preferences.html?raw'

// The Profile and settings screens of the apps (ADR 0029), drawn with the phone stylesheet (../screens.css).
// CLEANUP: the screens marked a11y todo carry axe findings the old mockups had (contrast of hints and past days, names of fields); fix them with the screens, then drop the todo.
const meta = {
  title: "Mobile/Screens/Profile and settings",
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: {}, docs: { canvas: { sourceState: 'none' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Body · height, nothing logged. */
export const BodyHeightEmpty: Story = {
  name: "Height, nothing logged",
  ...screen(screenBodyHeightEmpty),
  parameters: { mockup: { issue: undefined } },
}
/** Body · height (imperial). */
export const BodyHeight: Story = {
  name: "Height (imperial)",
  ...screen(screenBodyHeight),
  parameters: { mockup: { issue: undefined } },
}
/** Body · log a height (imperial). */
export const BodyLogHeightSheet: Story = {
  name: "Log a height (imperial)",
  ...screen(screenBodyLogHeightSheet),
  parameters: { mockup: { issue: undefined } },
}
/** Body · log a weight. */
export const BodyLogSheet: Story = {
  name: "Log a weight",
  ...screen(screenBodyLogSheet),
  parameters: { mockup: { issue: undefined } },
}
/** Body · weight, nothing logged. */
export const BodyWeightEmpty: Story = {
  name: "Weight, nothing logged",
  ...screen(screenBodyWeightEmpty),
  parameters: { mockup: { issue: undefined } },
}
/** Body · weight. */
export const BodyWeight: Story = {
  name: "Weight",
  ...screen(screenBodyWeight),
  parameters: { mockup: { issue: undefined } },
}
/** Settings · devices. */
export const DevicesList: Story = {
  name: "Devices",
  ...screen(screenDevicesList),
  parameters: { a11y: { test: 'todo' }, mockup: { issue: undefined } },
}
/** Profile · body figure. */
export const ProfileBodyFigureSheet: Story = {
  name: "Body figure",
  ...screen(screenProfileBodyFigureSheet),
  parameters: { a11y: { test: 'todo' }, mockup: { issue: undefined } },
}
/** Profile · edit. */
export const ProfileEdit: Story = {
  name: "Edit",
  ...screen(screenProfileEdit),
  parameters: { mockup: { issue: undefined } },
}
/** Profile · experience. */
export const ProfileExperienceSheet: Story = {
  name: "Experience",
  ...screen(screenProfileExperienceSheet),
  parameters: { mockup: { issue: undefined } },
}
/** Profile · goals. */
export const ProfileGoalsSheet: Story = {
  name: "Goals",
  ...screen(screenProfileGoalsSheet),
  parameters: { mockup: { issue: undefined } },
}
/** Profile · pain or rehab. */
export const ProfilePainSheet: Story = {
  name: "Pain or rehab",
  ...screen(screenProfilePainSheet),
  parameters: { mockup: { issue: undefined } },
}
/** Profile · pronouns. */
export const ProfilePronounsSheet: Story = {
  name: "Pronouns",
  ...screen(screenProfilePronounsSheet),
  parameters: { mockup: { issue: undefined } },
}
/** Profile · sex assigned at birth. */
export const ProfileSexSheet: Story = {
  name: "Sex assigned at birth",
  ...screen(screenProfileSexSheet),
  parameters: { mockup: { issue: undefined } },
}
/** Settings · choose an app icon. */
export const SettingsAppIconSheet: Story = {
  name: "Choose an app icon",
  ...screen(screenSettingsAppIconSheet),
  parameters: { mockup: { issue: 177 } },
}
/** Settings · app icon entry. */
export const SettingsGeneral: Story = {
  name: "App icon entry",
  ...screen(screenSettingsGeneral),
  parameters: { mockup: { issue: 177 } },
}
/** Settings · appearance and units. */
export const SettingsPreferences: Story = {
  name: "Appearance and units",
  ...screen(screenSettingsPreferences),
  parameters: { mockup: { issue: undefined } },
}
