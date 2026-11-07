import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [vue(), tailwindcss()],
  server: {
    // The local server from ./gradlew :server:run, reached as /api during development.
    proxy: { '/api': { target: 'http://localhost:8080', rewrite: (path) => path.replace(/^\/api/, '') } },
  },
})
