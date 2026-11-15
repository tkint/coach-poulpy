import type { StorybookConfig } from '@storybook/vue3-vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'
import remarkGfm from 'remark-gfm'

const config: StorybookConfig = {
  framework: '@storybook/vue3-vite',
  stories: ['../stories/**/*.stories.ts', '../stories/**/*.mdx'],
  addons: [
    '@storybook/addon-a11y',
    // The docs pages hold their text as MDX, tables included (ADR 0032).
    { name: '@storybook/addon-docs', options: { mdxPluginOptions: { mdxCompileOptions: { remarkPlugins: [remarkGfm] } } } },
    '@storybook/addon-vitest',
    '@storybook/addon-mcp',
  ],
  // The Poulpy theme is built here, with the stories as a source, so a story may use a Tailwind utility.
  viteFinal: (viteConfig) => ({
    ...viteConfig,
    plugins: [...(viteConfig.plugins ?? []), vue(), tailwindcss()],
    // web/ui reads Poulpy's pictures from design/poulpy-3d, outside the web workspace.
    server: { ...viteConfig.server, fs: { allow: ['../..'] } },
  }),
}
export default config
