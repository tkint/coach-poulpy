// Runs every story as a test: its play function (interactions) and the a11y check, in Chromium.
import { defineConfig } from 'vitest/config'
import { storybookTest } from '@storybook/addon-vitest/vitest-plugin'
import { playwright } from '@vitest/browser-playwright'

export default defineConfig({
  plugins: [storybookTest({ configDir: '.storybook' })],
  test: {
    browser: { enabled: true, headless: true, // CI installs Playwright's Chromium; CHROMIUM_PATH points at one already on the machine.
    provider: playwright({ launchOptions: { executablePath: process.env.CHROMIUM_PATH || undefined } }), instances: [{ browser: 'chromium' }] },
  },
})
