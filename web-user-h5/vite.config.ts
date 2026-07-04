import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { networkInterfaces } from 'node:os';

function resolveDefaultUserApiTarget() {
  const preferredInterfaces = ['en0', 'en1'];
  const allInterfaces = networkInterfaces();
  const interfaceNames = [...preferredInterfaces, ...Object.keys(allInterfaces)];

  for (const interfaceName of interfaceNames) {
    const addresses = allInterfaces[interfaceName] ?? [];
    for (const address of addresses) {
      if (address.family === 'IPv4' && !address.internal) {
        return `http://${address.address}:18080`;
      }
    }
  }

  return 'http://127.0.0.1:18080';
}

const userApiTarget = process.env.VITE_DEV_API_TARGET ?? resolveDefaultUserApiTarget();

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5174,
    host: '0.0.0.0',
    proxy: {
      '/api': {
        target: userApiTarget,
        changeOrigin: true,
      },
    },
  },
});
