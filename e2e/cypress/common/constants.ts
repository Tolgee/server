export const HOST = Cypress.env('HOST') || 'http://localhost:8201';
export const PASSWORD = Cypress.env('DEFAULT_PASSWORD') || 'admin';
export const USERNAME = Cypress.env('DEFAULT_USERNAME') || 'admin';
export const API_URL = Cypress.env('API_URL') || 'http://localhost:8201';

// get CI_RELEASE env variable
const CI_RELEASE = process.env.CI_RELEASE === 'true';
export const GLOBAL_RETRIES = CI_RELEASE ? 10 : 0;
