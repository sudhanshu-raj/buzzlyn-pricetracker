import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    open: true // Automatically open browser
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  // This ensures the app handles client-side routing correctly
  build: {
    outDir: 'dist',
    assetsDir: 'assets'
  }
})

