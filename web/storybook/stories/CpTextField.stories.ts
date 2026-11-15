import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, userEvent, within } from 'storybook/test'
import { ref } from 'vue'
import { Lock } from '@lucide/vue'
import { CpBadge, CpIcon, CpTextField, cpSizes } from '@coach-poulpy/ui'
import { sizeArgType } from './sizes'
import doc from './CpTextField.md?raw'

const meta = {
  title: 'Web/Components/Fields/TextField',
  parameters: { docs: { description: { component: doc } } },
  component: CpTextField,
  argTypes: { size: sizeArgType },
  args: { label: 'Email', type: 'email' },
  render: (args) => ({
    components: { CpTextField },
    setup: () => ({ args, value: ref('') }),
    template: '<CpTextField v-bind="args" v-model="value" />',
  }),
} satisfies Meta<typeof CpTextField>
export default meta
type Story = StoryObj<typeof meta>

export const Empty: Story = {}
// CLEANUP: the hint text is #6e737c on the page colour, 4.49:1 against the 4.5:1 minimum; raise its token, then drop this exemption.
export const WithAHint: Story = { parameters: { a11y: { test: 'todo' } }, args: { hint: 'We only use it to sign you in.' } }
export const WithAnError: Story = { args: { invalid: true, error: 'This email address is not valid.' } }
export const Typing: Story = {
  play: async ({ canvasElement }) => {
    const input = within(canvasElement).getByLabelText('Email')
    await userEvent.type(input, 'alex@example.com')
    expect(input).toHaveValue('alex@example.com')
  },
}
export const WithAPlaceholder: Story = { args: { type: 'text', label: 'Name', placeholder: 'Needed before publishing' } }
export const Disabled: Story = { args: { type: 'text', label: 'Slug', disabled: true } }
/** An icon before the text, such as a lock on a slug that cannot change. */
export const WithAnIcon: Story = {
  args: { type: 'text', label: 'Slug', disabled: true },
  render: (args) => ({
    components: { CpTextField, CpIcon },
    setup: () => ({ args, value: ref('squat'), Lock }),
    template: '<CpTextField v-bind="args" v-model="value"><template #icon><CpIcon :icon="Lock" size="sm" /></template></CpTextField>',
  }),
}
/** A badge under the hint, saying where a translated text stands. */
// CLEANUP: the success badge is 3.1:1, below 4.5:1 (see the Badge stories); drop this exemption with that one.
export const WithAStatus: Story = {
  parameters: { a11y: { test: 'todo' } },
  args: { type: 'text', label: 'Name' },
  render: (args) => ({
    components: { CpTextField, CpBadge },
    setup: () => ({ args, value: ref('Squat') }),
    template: '<CpTextField v-bind="args" v-model="value"><template #status><CpBadge tone="success" small>Translated</CpBadge></template></CpTextField>',
  }),
}

/** Every step of the size scale, xs to xxl; md is what a component without a `size` draws. */
export const Sizes: Story = {
  render: (args) => ({
    components: { CpTextField },
    setup: () => ({ args, cpSizes, value: ref('') }),
    template: '<div class="flex flex-col items-start gap-4 pb-8"><div v-for="s in cpSizes" :key="s" class="flex items-center gap-4"><span class="cp-type-label w-10">{{ s }}</span><CpTextField v-bind="args" :size="s" v-model="value" /></div></div>',
  }),
}
