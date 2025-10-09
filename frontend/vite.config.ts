import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react-swc'
import checker from 'vite-plugin-checker'

export default defineConfig({
    plugins: [
        react(),
        checker({ typescript: true }),
    ],
    server: {
        host: true,
        port: 5173,
        strictPort: true,
        open: true,
        watch: { usePolling: true , interval: 100 },
        hmr: { clientPort: 5173 },

        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
            }
        }
    }
})
