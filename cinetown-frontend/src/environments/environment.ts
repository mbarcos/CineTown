export const environment = {
  apiUrl: (globalThis as typeof globalThis & { CINETOWN_API_URL?: string }).CINETOWN_API_URL
    || 'http://localhost:8081',
  production: false
}
