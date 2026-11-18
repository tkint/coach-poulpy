import type { Meta, StoryObj } from '@storybook/vue3-vite'
import AdminAccountsScreen, { type Account } from './AdminAccountsScreen.vue'

// The admin's accounts (issue #336). The three Confirm stories are drafts (issue #382): confirming an unconfirmed email by hand.
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

/** Draft (#382): an unconfirmed account's details offer "Confirm email" beside the other actions. */
export const DetailsNotConfirmed: Story = { tags: ['draft'], parameters: { mockup: { issue: 382 } }, args: { ...base, selected: 'tom@example.com' } }
/** Draft (#382): the confirmation dialog; the admin vouches for an address nobody proved. */
export const ConfirmEmail: Story = { tags: ['draft'], parameters: { mockup: { issue: 382 } }, args: { ...base, selected: 'tom@example.com', confirming: true } }
/** Draft (#382): after confirming, the row reads Confirmed, the button is gone and a note says the account can sign in. */
export const EmailConfirmed: Story = {
  tags: ['draft'],
  parameters: { mockup: { issue: 382 } },
  args: { ...base, accounts: accounts.map((a) => (a.email === 'tom@example.com' ? { ...a, confirmed: true } : a)), selected: 'tom@example.com', justConfirmed: true },
}
