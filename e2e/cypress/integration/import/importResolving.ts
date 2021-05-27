import {cleanImportData, generateImportData, login} from "../../common/apiCalls";
import 'cypress-file-upload';
import {gcy} from "../../common/shared";
import {findResolutionRow, getLanguageRow, visitImport} from "../../common/import";

describe('Import Resolving', () => {
    beforeEach(() => {
        cleanImportData()
        generateImportData().then(importData => {
            login("franta")
            visitImport(importData.body.repository.id);
        })
    })

    it("shows correct initial data", () => {
        getLanguageRow("multilang.json (en)").findDcy("import-result-resolve-button").click()
        gcy("import-resolution-dialog-resolved-count").should("have.text", "0")
        gcy("import-resolution-dialog-conflict-count").should("have.text", "4")
        gcy("import-resolution-dialog-show-resolved-switch").find("input").should("be.checked")
        gcy("import-resolution-dialog-data-row").should("have.length", 4)
        gcy("import-resolution-dialog-data-row").should("contain.text", "What a text")
    })

    it("resolves row (one by one)", () => {
        getLanguageRow("multilang.json (en)").findDcy("import-result-resolve-button").click()
        gcy("import-resolution-dialog-data-row").contains("Overridden").click()
        cy.xpath("//*[@data-cy-selected]").should("have.length", 1)
        findResolutionRow("what a key").findDcy("import-resolution-dialog-existing-translation")
            .should("not.have.attr", "data-cy-selected")
        findResolutionRow("what a key").findDcy("import-resolution-dialog-new-translation")
            .should("have.attr", "data-cy-selected")

        findResolutionRow("what a nice key").contains("What a text").click()
        cy.xpath("//*[@data-cy-selected]").should("have.length", 2)
        findResolutionRow("what a nice key").findDcy("import-resolution-dialog-new-translation")
            .should("not.have.attr", "data-cy-selected")
        findResolutionRow("what a nice key").findDcy("import-resolution-dialog-existing-translation")
            .should("have.attr", "data-cy-selected")

        gcy("import-resolution-dialog-resolved-count").should("have.text", "2")
    })

    it("accept all new", () => {
        getLanguageRow("multilang.json (en)").findDcy("import-result-resolve-button").click()
        gcy("import-resolution-dialog-accept-imported-button").click()
        cy.xpath("//*[@data-cy-selected]").should("have.length", 4)
        gcy("import-resolution-dialog-new-translation").each(($el) => {
            cy.wrap($el).should("have.attr", "data-cy-selected")
        })
        gcy("import-resolution-dialog-resolved-count").should("have.text", "4")
    })

    it("accept all old", () => {
        getLanguageRow("multilang.json (en)").findDcy("import-result-resolve-button").click()
        gcy("import-resolution-dialog-accept-old-button").click()
        cy.xpath("//*[@data-cy-selected]").should("have.length", 4)
        gcy("import-resolution-dialog-existing-translation").each(($el) => {
            cy.wrap($el).should("have.attr", "data-cy-selected")
        })
        gcy("import-resolution-dialog-resolved-count").should("have.text", "4")
    })

    after(() => {
        cleanImportData()
    })

})
