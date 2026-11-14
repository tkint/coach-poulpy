import type { Meta, StoryObj } from '@storybook/vue3-vite'
import AdminAccountsScreen, { type Account } from './AdminAccountsScreen.vue'

// The admin's accounts (issue #336). Was design/mockups/agreed/web/admin-accounts*.html.
const meta = {
  title: 'Web/Screens/Admin/Accounts',
  component: AdminAccountsScreen,
  tags: ['!autodocs', 'agreed'],
  // CLEANUP: the admin shell's CpDrawer puts aria-label on a <label> (axe aria-prohibited-attr), as in the Drawer stories.
  parameters: { layout: 'fullscreen', mockup: { issue: 336 }, a11y: { test: 'todo' } },
} satisfies Meta<typeof AdminAccountsScreen>
export default meta
type Story = StoryObj<typeof meta>

const accounts: Account[] = [
  { email: 'lea@example.com', role: 'Member', confirmed: true, disabled: false, created: '9 Nov 2026' },
  { email: 'tom@example.com', role: 'Member', confirmed: false, disabled: false, created: '8 Nov 2026' },
  { email: 'ana@example.com', role: 'Admin', confirmed: true, disabled: false, created: '2 Nov 2026' },
  { email: 'sam@example.com', role: 'Member', confirmed: true, disabled: true, created: '28 Oct 2026' },
  { email: 'noa@example.com', role: 'Member', confirmed: true, disabled: false, created: '21 Oct 2026' },
  { email: 'ines@example.com', role: 'Member', confirmed: true, disabled: false, created: '14 Oct 2026' },
  { email: 'hugo@example.com', role: 'Member', confirmed: true, disabled: false, created: '9 Oct 2026' },
  { email: 'zoe@example.com', role: 'Member', confirmed: true, disabled: false, created: '2 Oct 2026' },
]
const base = { accounts, showing: 'Showing 1 to 8 of 21 accounts' }

export const List: Story = { args: base }
export const Filtered: Story = { args: { accounts: [accounts[3]], showing: 'Showing 1 to 1 of 1 account', pages: 1, signIn: 'disabled', sort: 'email', order: 'asc' } }
export const NoMatch: Story = { args: { accounts: [], search: 'nobody' } }
export const Details: Story = { args: { ...base, selected: 'lea@example.com' } }
export const DetailsDisabled: Story = { args: { ...base, accounts: [{ ...accounts[0], disabled: true }, ...accounts.slice(1)], selected: 'lea@example.com', disabledSince: '9 Nov, 09:02' } }
export const Delete: Story = { args: { ...base, selected: 'lea@example.com', deleting: true, typed: 'lea@exam' } }
