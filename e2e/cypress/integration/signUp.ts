import { HOST } from '../common/constants';
import { getInput } from '../common/xPath';
import {
  createProject,
  deleteAllEmails,
  deleteUserWithEmailVerification,
  disableEmailVerification,
  enableEmailVerification,
  getParsedEmailVerification,
  getUser,
  login,
} from '../common/apiCalls';
import { assertMessage, selectInProjectMenu } from '../common/shared';
import { loginWithFakeGithub } from './login.spec';

const createProjectWithInvitation = (
  name = 'Test'
): Cypress.Chainable<{
  projectId: string;
  invitationLink: string;
}> => {
  return login()
    .then(() =>
      createProject({
        name,
        languages: [
          {
            tag: 'en',
            name: 'English',
            originalName: 'English',
            flagEmoji: '🇬🇧',
          },
        ],
      })
    )
    .then((r) => {
      cy.visit(`${HOST}/projects/${r.body.id}`);
      selectInProjectMenu('Invite user');
      cy.gcy('invite-generate-button').click();
      return cy
        .gcy('invite-generate-input-code')
        .find('textarea')
        .invoke('val')
        .then((invitationLink: string) => {
          window.localStorage.removeItem('jwtToken');
          return { projectId: r.body.id, invitationLink };
        });
    });
};

const TEST_USERNAME = 'johndoe@doe.com';
context('Login', () => {
  beforeEach(() => {
    cy.visit(HOST + '/sign_up');
    deleteUserWithEmailVerification(TEST_USERNAME);
    deleteAllEmails();
    enableEmailVerification();
  });

  afterEach(() => {
    //enableEmailVerification()
    deleteUserWithEmailVerification(TEST_USERNAME);
  });

  it('Will sign up', () => {
    fillAndSubmitForm();
    cy.contains(
      'Thank you for signing up. To verify your e-mail please follow instructions sent to provided e-mail address.'
    ).should('be.visible');
    getUser(TEST_USERNAME).then((u) => {
      expect(u[0]).be.equal(TEST_USERNAME);
      expect(u[1]).be.not.null;
    });
    getParsedEmailVerification().then((r) => {
      cy.wrap(r.fromAddress).should('contain', 'no-reply@tolgee.io');
      cy.wrap(r.toAddress).should('contain', TEST_USERNAME);
      cy.visit(r.verifyEmailLink);
      assertMessage('E-mail was verified.');
    });
  });

  it('will sign up without email verification', () => {
    disableEmailVerification();
    fillAndSubmitForm();
    assertMessage('Thanks for your sign up!');
    cy.gcy('global-base-view-title').contains('Projects');
  });

  it('will sign up with project invitation code', () => {
    disableEmailVerification();
    createProjectWithInvitation().then(({ invitationLink }) => {
      cy.visit(HOST + '/sign_up');
      fillAndSubmitForm();
      cy.contains('Projects').should('be.visible');
      cy.visit(invitationLink);
      assertMessage('Invitation successfully accepted');
    });
  });

  it('will remember code after sign up', () => {
    disableEmailVerification();
    createProjectWithInvitation('Crazy project').then(({ invitationLink }) => {
      cy.visit(invitationLink);
      assertMessage('Log in or sign up first please');
      cy.visit(HOST + '/sign_up');
      fillAndSubmitForm();
      assertMessage('Thanks for your sign up!');
      cy.contains('Crazy project').should('be.visible');
    });
  });

  it('will work with github signup', () => {
    disableEmailVerification();
    createProjectWithInvitation('Crazy project').then(({ invitationLink }) => {
      cy.visit(HOST + '/login');
      loginWithFakeGithub();
      cy.contains('Projects').should('be.visible');
      cy.visit(invitationLink);
      cy.contains('Crazy project').should('be.visible');
    });
  });

  it('will remember code after github signup', () => {
    disableEmailVerification();
    createProjectWithInvitation('Crazy project').then(({ invitationLink }) => {
      cy.visit(invitationLink);
      assertMessage('Log in or sign up first please');
      cy.intercept('/api/public/authorize_oauth/github/*').as('GithubSignup');
      loginWithFakeGithub();
      cy.wait('@GithubSignup').then((interception) => {
        assert.isTrue(interception.request.url.includes('invitationCode'));
      });
    });
  });
});

const fillAndSubmitForm = () => {
  cy.xpath(getInput('name')).type('Test user');
  cy.xpath(getInput('email')).type(TEST_USERNAME);
  cy.xpath(getInput('password')).type('password');
  cy.xpath(getInput('passwordRepeat')).type('password');
  cy.contains('Submit').click();
};
