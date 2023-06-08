import { waitForGlobalLoading } from '../../common/loading';
import { namespaces } from '../../common/apiCalls/testData/testData';
import { login } from '../../common/apiCalls/common';
import {
  createTranslation,
  visitTranslations,
} from '../../common/translations';
import {
  confirmStandard,
  gcy,
  getPopover,
  selectInSelect,
} from '../../common/shared';
import { selectNamespace } from '../../common/namespace';

describe('namespaces in translations', () => {
  beforeEach(() => {
    namespaces.clean({ failOnStatusCode: false });
    namespaces
      .generateStandard()
      .then((r) => r.body)
      .then(({ users, projects }) => {
        login(users[0].username);
        const testProject = projects.find(
          ({ name }) => name === 'test_project'
        );
        visitTranslations(testProject.id);
      });
    waitForGlobalLoading();
  });

  afterEach(() => {
    namespaces.clean({ failOnStatusCode: false, timeout: 60000 });
    cy.wait(10000);
  });

  it('displays keys with namespaces correctly', () => {
    gcy('translations-namespace-banner').contains('ns-1').should('be.visible');
    gcy('translations-namespace-banner').contains('ns-2').should('be.visible');
  });

  it('displays <none>', () => {
    createTranslation('new-key', undefined, undefined, 'new-ns');
    gcy('translations-namespace-banner')
      .contains('new-ns')
      .should('be.visible');
    gcy('translations-namespace-banner')
      .contains('<none>')
      .should('be.visible');
  });

  it('filters by multiple namespaces', () => {
    gcy('translations-key-count').contains('5').should('be.visible');
    selectInSelect(gcy('translations-filter-select'), 'Namespaces');
    getPopover().contains('ns-1').click();
    getPopover().contains('ns-2').click();
    cy.focused().type('{Esc}');
    cy.focused().type('{Esc}');
    gcy('translations-key-count').contains('3').should('be.visible');
  });

  it('filters by empty namespace', () => {
    gcy('translations-key-count').contains('5').should('be.visible');
    selectInSelect(gcy('translations-filter-select'), 'Namespaces');
    getPopover().contains('<none>').click();
    cy.focused().type('{Esc}');
    cy.focused().type('{Esc}');
    gcy('translations-key-count').contains('2').should('be.visible');
  });

  it('edits namespaced translation correctly', () => {
    gcy('translations-namespace-banner')
      .should('exist')
      .nextUntilDcy('translations-namespace-banner')
      .findDcy('translations-table-cell')
      .first()
      .click();
    cy.gcy('global-editor').type(' edited translation').type('{enter}');
    waitForGlobalLoading();
    cy.gcy('translations-table-cell')
      .contains('edited translation')
      .should('be.visible');
  });

  it('updates namespace correctly', () => {
    gcy('translations-namespace-banner')
      .nextUntilDcy('translations-namespace-banner')
      .findDcy('translations-table-cell')
      .first()
      .click();

    selectNamespace('new-ns');

    cy.gcy('translations-cell-save-button').click();

    waitForGlobalLoading();

    cy.gcy('translations-namespace-banner')
      .contains('new-ns')
      .should('be.visible');
  });

  it('filters by clicking on banner', () => {
    gcy('translations-key-count').contains('5').should('be.visible');
    filterByNsBanner('ns-1');
    gcy('translations-key-count').contains('2').should('be.visible');
    removeFilterNsBanner('ns-1');
    gcy('translations-key-count').contains('5').should('be.visible');
  });

  it('rename namespace', () => {
    gcy('translations-key-count').contains('5').should('be.visible');
    gcy('namespaces-banner-content')
      .contains('ns-1')
      .closestDcy('translations-namespace-banner')
      .findDcy('namespaces-banner-menu-button')
      .click();

    gcy('namespaces-banner-menu-option').contains('Rename namespace').click();

    gcy('namespaces-rename-text-field')
      .click()
      .clear()
      .type('renamed-namespace');

    gcy('namespaces-rename-confirm').click();

    confirmStandard();

    gcy('translations-namespace-banner')
      .contains('renamed-namespace')
      .should('exist');
  });

  it("franta doesn't have permission to rename namespace", () => {
    login('franta');
    cy.reload();
    gcy('translations-key-count').contains('5').should('be.visible');
    gcy('namespaces-banner-content')
      .contains('ns-1')
      .closestDcy('translations-namespace-banner')
      .findDcy('namespaces-banner-menu-button')
      .click();

    gcy('namespaces-banner-menu-option')
      .contains('Rename namespace')
      .should('not.exist');
  });

  function filterByNsBanner(namespace: string) {
    gcy('translations-namespace-banner').contains(namespace).click();
    getPopover().contains('Filter by namespace').click();
  }

  function removeFilterNsBanner(namespace: string) {
    gcy('translations-namespace-banner').contains(namespace).click();
    getPopover().contains('Remove filter').click();
  }
});
