/// <reference types="cypress" />
import {HOST, PASSWORD, USERNAME} from "../common/constants";
import {getAnyContainingText} from "../common/xPath";
import {createUser, deleteAllEmails, getParsedResetPasswordEmail, login} from "../common/apiCalls";
import {assertMessage, getPopover} from "../common/shared";

context('Login', () => {
    beforeEach(() => {
        cy.visit(HOST);
    });

    it('Will login', () => {
        cy.xpath('//input[@name="username"]')
            .type(USERNAME).should('have.value', USERNAME);
        cy.xpath('//input[@name="password"]')
            .type(PASSWORD).should('have.value', PASSWORD);
        cy.xpath("//button//*[text() = 'Login']").click().should("not.exist");
        cy.xpath("//*[@aria-controls='user-menu']").should("be.visible");
    });

    it('Will fail on invalid credentials', () => {
        cy.xpath('//input[@name="username"]')
            .type("aaaa").should('have.value', "aaaa");
        cy.xpath('//input[@name="password"]')
            .type(PASSWORD).should('have.value', PASSWORD);
        cy.xpath("//button//*[text() = 'Login']").should("be.visible").click();
        cy.xpath(getAnyContainingText("Login")).should("be.visible");
        cy.contains("Invalid credentials").should("be.visible");
    });

    it('Will login with github', () => {
        cy.intercept("https://github.com/login/oauth/**", {
            statusCode: 200,
            body: "Fake GitHub"
        })
        cy.contains("Github login").click()
        cy.contains("Fake GitHub").should("be.visible")
        cy.visit(HOST + "/login/auth_callback/github?scope=user%3Aemail&code=this_is_dummy_code")
        cy.contains("Repositories").should("be.visible")
    });

    it('Will logout', () => {
        login()
        cy.reload();
        cy.xpath("//*[@aria-controls='user-menu']").click();
        getPopover().contains("Logout").click();
        cy.contains("Login").should("be.visible");

    });

    it("will reset password", () => {
        deleteAllEmails()
        let username = "test@testuser.com";
        createUser(username)
        cy.contains("Reset password").click()
        cy.xpath("//*[@name='email']").type(username)
        cy.contains("Send request").click()
        cy.contains("Request successfully sent! If you are signed up using this e-mail," +
            " you will receive an e-mail with a link for password reset. Check your mail box.")
        getParsedResetPasswordEmail().then(r => {
            cy.visit(r.resetLink)
        })
        let newPassword = "new_password";
        cy.xpath("//*[@name='password']").type(newPassword)
        cy.xpath("//*[@name='passwordRepeat']").type(newPassword)
        cy.contains("Save new password").click()
        assertMessage("Password successfully reset")
        login(username, newPassword)
    })
});
