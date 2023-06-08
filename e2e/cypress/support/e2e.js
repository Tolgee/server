// ***********************************************************
// This example support/index.js is processed and
// loaded automatically before your test files.
//
// This is a great place to put global configuration and
// behavior that modifies Cypress.
//
// You can change the location of this file or turn off
// automatically serving support files with the
// 'supportFile' configuration option.
//
// You can read more here:
// https://on.cypress.io/configuration
// ***********************************************************

// Import commands.js using ES2015 syntax:
import './commands';
import 'cypress-promise/register';
import { internalFetch } from '../common/apiCalls/common';

require('cypress-xpath');

// Alternatively you can use CommonJS syntax:
// require('./commands')

Cypress.on('uncaught:exception', (err, runnable) => {
  if (
    err.hasOwnProperty('CUSTOM_VALIDATION') ||
    err.hasOwnProperty('STANDARD_VALIDATION')
  ) {
    return false;
  }
  if (err.hasOwnProperty('code') && typeof err.code == 'string') {
    return false;
  }
});

Cypress.on('window:before:load', (win) => {
  win.localStorage.setItem('__tolgee_currentLanguage', 'en');
});

before(() => {
  // turn on GRANULAR_PERMISSIONS
  internalFetch('features/toggle?feature=GRANULAR_PERMISSIONS&enabled=true', {
    method: 'put',
  });
});

// observe pending requests
let pendingRequests = 0;
beforeEach(() => {
  pendingRequests = 0;
  cy.intercept('**', (req) => {
    pendingRequests += 1;
    return req.continue((res) => {
      pendingRequests -= 1;
      return res;
    });
  }).as('request');
});

afterEach(() => {
  // wait for all requests to finish
  // before going to next test
  if (pendingRequests > 0) {
    const tet = cy.wait('@request');
  }
});
