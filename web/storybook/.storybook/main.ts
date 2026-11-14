import type { StorybookConfig } from '@storybook/vue3-vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'

const config: StorybookConfig = {
  framework: '@storybook/vue3-vite',
  stories: ['../stories/**/*.stories.ts', '../stories/**/*.mdx'],
  addons: ['@storybook/addon-a11y', '@storybook/addon-docs', '@storybook/addon-vitest'],
  // The Poulpy theme is built here, with the stories as a source, so a story may use a Tailwind utility.
  viteFinal: (viteConfig) => ({
    ...viteConfig,
    plugins: [...(viteConfig.plugins ?? []), vue(), tailwindcss()],
    // web/ui reads Poulpy's drawings from design/poulpy, outside the web workspace.
    server: { ...viteConfig.server, fs: { allow: ['../..'] } },
  }),
}
export default config
