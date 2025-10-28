import {defineConfig} from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
    plugins: [vue()],
    resolve: {
        alias: {
            '@': '/src'
        }
    },
    server: {
        proxy: {
            '/api': {
                target: 'http://localhost:8099',
                changeOrigin: true, // needed for virtual hosted sites
                ws: true // proxy websockets
            },
            '/oauth2': {
                target: 'http://localhost:8099',
                changeOrigin: true, // needed for virtual hosted sites
                ws: true // proxy websockets
            }
        },
        port: 8080,
        host: '0.0.0.0'
    },
    build: {
        outDir: 'target/dist',
        assetsDir: 'static'
    }
});