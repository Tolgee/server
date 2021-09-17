import {
  createApiKey,
  createTestProject,
  deleteAllProjectApiKeys,
  login,
} from '../../common/apiCalls';
import { HOST } from '../../common/constants';
import 'cypress-file-upload';
import { selectInSelect } from '../../common/shared';

describe('Integrate view', () => {
  let projectId: number;

  beforeEach(() => {
    login().then(() => {
      createTestProject().then((p) => {
        projectId = p.body.id;
        cy.visit(`${HOST}/projects/${p.body.id}/integrate`);
      });
    });
  });

  it('gets to integrate view', () => {
    cy.gcy('integrate-navigation-title').should('be.visible');
  });

  describe('Step 1 | Choose your weapon', () => {
    beforeEach(() => {});

    it('has visible label', () => {
      cy.gcy('integrate-choose-your-weapon-step-label').should('be.visible');
    });

    it('has visible content', () => {
      cy.gcy('integrate-choose-your-weapon-step-content').should('be.visible');
    });

    it('contains all weapons', () => {
      cy.gcy('integrate-choose-your-weapon-step-content')
        .should('contain', 'React')
        .should('contain', 'Angular')
        .should('contain', 'Next.js')
        .should('contain', 'Gatsby')
        .should('contain', 'Php')
        .should('contain', 'Rest')
        .should('contain', 'Web')
        .should('contain', 'JS (NPM)');
    });

    it('weapon can be selected', () => {
      cy.gcy('integrate-choose-your-weapon-step-content')
        .contains('React')
        .click();
      cy.gcy('integrate-select-api-key-step-content').should('be.visible');
    });

    it('weapon is stored, so its selected after refresh', () => {
      cy.gcy('integrate-choose-your-weapon-step-content')
        .contains('React')
        .click();
      cy.reload();
      cy.gcy('integrate-select-api-key-step-content').should('be.visible');
    });
  });

  describe('Step 2 | Select API key', () => {
    beforeEach(() => {
      cy.gcy('integrate-choose-your-weapon-step-content')
        .contains('Angular')
        .click();
    });

    it('has visible label', () => {
      cy.gcy('integrate-select-api-key-step-label').should('be.visible');
    });

    it('has visible content', () => {
      cy.gcy('integrate-select-api-key-step-content').should('be.visible');
    });

    it('contains the selector', () => {
      cy.gcy('integrate-api-key-selector-select').should('be.visible');
    });

    describe('new api key', () => {
      beforeEach(() => {
        deleteAllProjectApiKeys(projectId);
      });

      it('can create new API key when no API key exists', () => {
        createNewApiKey();
        getApiKeySelectValue().should('gt', 1000000);
      });
    });

    it.only('can create new API key when some API keys exist', () => {
      createApiKeysAndSelectOne(projectId).then((created) => {
        createNewApiKey();
        getApiKeySelectValue().then((selected) => {
          cy.wrap(created).its('id').should('not.eq', selected);
        });
      });
    });

    it('can use existing API key', () => {
      createApiKeysAndSelectOne(projectId).then((v) => {
        getApiKeySelectValue().should('eq', v.id);
      });
    });

    afterEach(() => {
      deleteAllProjectApiKeys(projectId);
    });
  });
});

const getApiKeySelectValue = () => {
  return cy
    .gcy('integrate-api-key-selector-select-input')
    .invoke('val')
    .should('not.be.empty')
    .then((v) => parseInt(v as string));
};

const createApiKeysAndSelectOne = (projectId: number) => {
  createApiKey({ projectId: projectId, scopes: ['translations.edit'] });
  return createApiKey({
    projectId: projectId,
    scopes: ['translations.edit'],
  }).then((v) =>
    cy
      .reload()
      .then(() =>
        selectInSelect(cy.gcy('integrate-api-key-selector-select'), v.key).then(
          () => v
        )
      )
  );
};

const createNewApiKey = () => {
  cy.gcy('integrate-api-key-selector-select').click();
  cy.gcy('integrate-api-key-selector-create-new-item').click();
  cy.gcy('global-form-save-button').click();
};
