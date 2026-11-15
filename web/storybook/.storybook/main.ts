import type { StorybookConfig } from '@storybook/vue3-vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'

const config: StorybookConfig = {
  framework: '@storybook/vue3-vite',
  stories: ['../stories/**/*.stories.ts', '../stories/**/*.mdx'],
  addons: ['@storybook/addon-a11y', '@storybook/addon-docs', '@storybook/addon-vitest', '@storybook/addon-mcp'],
  // The Poulpy theme is built here, with the stories as a source, so a story may use a Tailwind utility.
  viteFinal: (viteConfig) => ({
    ...viteConfig,
    plugins: [...(viteConfig.plugins ?? []), vue(), tailwindcss()],
    // web/ui reads Poulpy's pictures from design/poulpy-3d, outside the web workspace.
    server: { ...viteConfig.server, fs: { allow: ['../..'] } },
  }),
}
export default config
