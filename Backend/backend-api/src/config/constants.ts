// Global constants used across the application

// Keep trailing slash to match Nest's setGlobalPrefix current usage
export const GLOBAL_PREFIX = 'maian';

// Result: '/maian/auth/refresh-token'
export const REFRESH_TOKEN_COOKIE_PATH = `/${GLOBAL_PREFIX}/auth/refresh-token`;
