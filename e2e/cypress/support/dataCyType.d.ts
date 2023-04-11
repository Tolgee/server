declare namespace DataCy {
    export type Value = 
        "account-security-initial-password-set" |
        "account-security-set-password-instructions-sent" |
        "activity-compact" |
        "activity-detail" |
        "add-box" |
        "administration-access-message" |
        "administration-debug-customer-account-message" |
        "administration-debug-customer-exit-button" |
        "administration-frame" |
        "administration-organizations-list-item" |
        "administration-organizations-projects-button" |
        "administration-organizations-settings-button" |
        "administration-user-debug-account" |
        "administration-user-delete-user" |
        "administration-user-disable-user" |
        "administration-user-enable-user" |
        "administration-user-role-select" |
        "administration-users-list-item" |
        "api-key-expiry-info" |
        "api-key-list-item" |
        "api-key-list-item-delete-button" |
        "api-key-list-item-description" |
        "api-key-list-item-last-used" |
        "api-key-list-item-new-token-input" |
        "api-key-list-item-regenerate-button" |
        "api-keys-create-edit-dialog" |
        "api-keys-project-select-item" |
        "auto-avatar-img" |
        "avatar-image" |
        "avatar-menu-open-button" |
        "avatar-remove-button" |
        "avatar-upload-button" |
        "avatar-upload-file-input" |
        "base-language-select" |
        "billing-actual-extra-credits" |
        "billing-actual-period" |
        "billing-actual-period-end" |
        "billing-extra-credits-buy" |
        "billing-limit-exceeded-popover" |
        "billing-period-switch" |
        "billing-plan" |
        "billing-plan-action-button" |
        "billing-plan-subtitle" |
        "billing-plan-title" |
        "billing-subscriptions-cloud-button" |
        "billing-subscriptions-self-hosted-ee-button" |
        "billing-upgrade-preview-confirm-button" |
        "comment" |
        "comment-menu" |
        "comment-menu-delete" |
        "comment-menu-needs-resolution" |
        "comment-resolve" |
        "comment-text" |
        "create-project-language-add-button" |
        "dashboard-projects-list-item" |
        "delete-user-button" |
        "dropzone" |
        "dropzone-inner" |
        "edit-pat-dialog-content" |
        "edit-pat-dialog-description-input" |
        "edit-pat-dialog-title" |
        "expiration-date-field" |
        "expiration-date-picker" |
        "expiration-select" |
        "export-format-selector" |
        "export-format-selector-item" |
        "export-language-selector" |
        "export-language-selector-item" |
        "export-namespace-selector" |
        "export-namespace-selector-item" |
        "export-nested-selector" |
        "export-state-selector" |
        "export-state-selector-item" |
        "export-submit-button" |
        "former-user-name" |
        "generate-api-key-dialog-description-input" |
        "generate-pat-dialog-content" |
        "generate-pat-dialog-description-input" |
        "generate-pat-dialog-title" |
        "global-base-view-content" |
        "global-base-view-title" |
        "global-confirmation-cancel" |
        "global-confirmation-confirm" |
        "global-confirmation-hard-mode-text-field" |
        "global-editor" |
        "global-empty-list" |
        "global-form-cancel-button" |
        "global-form-save-button" |
        "global-form-select" |
        "global-list-items" |
        "global-list-pagination" |
        "global-list-search" |
        "global-loading" |
        "global-paginated-list" |
        "global-plus-button" |
        "global-search-field" |
        "global-snackbars" |
        "global-user-menu-button" |
        "import-conflict-resolution-dialog" |
        "import-conflicts-not-resolved-dialog" |
        "import-conflicts-not-resolved-dialog-cancel-button" |
        "import-conflicts-not-resolved-dialog-resolve-button" |
        "import-file-error" |
        "import-file-error-collapse-button" |
        "import-file-error-more-less-button" |
        "import-file-input" |
        "import-file-issues-button" |
        "import-file-issues-dialog" |
        "import-resolution-dialog-accept-imported-button" |
        "import-resolution-dialog-accept-old-button" |
        "import-resolution-dialog-close-button" |
        "import-resolution-dialog-conflict-count" |
        "import-resolution-dialog-data-row" |
        "import-resolution-dialog-existing-translation" |
        "import-resolution-dialog-key-name" |
        "import-resolution-dialog-new-translation" |
        "import-resolution-dialog-resolved-count" |
        "import-resolution-dialog-show-resolved-switch" |
        "import-resolution-dialog-translation-check" |
        "import-resolution-dialog-translation-loading" |
        "import-resolution-translation-expand-button" |
        "import-result-delete-language-button" |
        "import-result-file-cell" |
        "import-result-file-warnings" |
        "import-result-language-menu-cell" |
        "import-result-namespace-cell" |
        "import-result-resolve-button" |
        "import-result-resolved-conflicts-cell" |
        "import-result-row" |
        "import-result-show-all-translations-button" |
        "import-result-total-count-cell" |
        "import-row-language-select-clear-button" |
        "import-row-language-select-form-control" |
        "import-show-data-dialog" |
        "import_apply_import_button" |
        "import_cancel_import_button" |
        "integrate-api-key-selector-create-new-item" |
        "integrate-api-key-selector-item" |
        "integrate-api-key-selector-select" |
        "integrate-api-key-selector-select-input" |
        "integrate-choose-your-weapon-step-content" |
        "integrate-choose-your-weapon-step-label" |
        "integrate-go-to-docs-button" |
        "integrate-guide" |
        "integrate-select-api-key-step-content" |
        "integrate-select-api-key-step-label" |
        "integrate-weapon-selector-button" |
        "invitation-dialog-close-button" |
        "invitation-dialog-input-field" |
        "invitation-dialog-invite-button" |
        "invitation-dialog-role-button" |
        "invitation-dialog-type-email-button" |
        "invitation-dialog-type-link-button" |
        "invite-generate-button" |
        "language-delete-button" |
        "language-modify-form" |
        "languages-auto-machine-translation" |
        "languages-auto-translation-memory" |
        "languages-create-autocomplete-field" |
        "languages-create-autocomplete-suggested-option" |
        "languages-create-cancel-prepared-button" |
        "languages-create-customize-button" |
        "languages-create-submit-button" |
        "languages-flag-selector-open-button" |
        "languages-modify-apply-button" |
        "languages-modify-cancel-button" |
        "languages-prepared-language-box" |
        "login-button" |
        "mfa-disable-button" |
        "mfa-disable-dialog" |
        "mfa-disable-dialog-content" |
        "mfa-disable-dialog-password-input" |
        "mfa-disable-dialog-title" |
        "mfa-enable-button" |
        "mfa-enable-dialog" |
        "mfa-enable-dialog-content" |
        "mfa-enable-dialog-otp-input" |
        "mfa-enable-dialog-password-input" |
        "mfa-enable-dialog-title" |
        "mfa-enable-dialog-totp-key" |
        "mfa-recovery-button" |
        "mfa-recovery-codes-dialog" |
        "mfa-recovery-codes-dialog-close" |
        "mfa-recovery-codes-dialog-content" |
        "mfa-recovery-codes-dialog-password-input" |
        "mfa-recovery-codes-dialog-title" |
        "namespaces-banner-content" |
        "namespaces-banner-menu" |
        "namespaces-banner-menu-button" |
        "namespaces-banner-menu-option" |
        "namespaces-rename-cancel" |
        "namespaces-rename-confirm" |
        "namespaces-rename-text-field" |
        "namespaces-select-cancel" |
        "namespaces-select-confirm" |
        "namespaces-select-text-field" |
        "namespaces-selector" |
        "navigation-item" |
        "organization-address-part-field" |
        "organization-description-field" |
        "organization-invitation-cancel-button" |
        "organization-invitation-copy-button" |
        "organization-invitation-item" |
        "organization-member-item" |
        "organization-member-leave-button" |
        "organization-members-remove-user-button" |
        "organization-name-field" |
        "organization-profile" |
        "organization-profile-delete-button" |
        "organization-profile-leave-button" |
        "organization-role-menu" |
        "organization-role-menu-button" |
        "organization-role-select-item" |
        "organization-side-menu" |
        "organization-switch" |
        "organization-switch-item" |
        "organization-switch-new" |
        "organization-switch-search" |
        "pat-expiry-info" |
        "pat-list-item" |
        "pat-list-item-alert" |
        "pat-list-item-delete-button" |
        "pat-list-item-description" |
        "pat-list-item-last-used" |
        "pat-list-item-new-token-input" |
        "pat-list-item-regenerate-button" |
        "permissions-advanced-checkbox" |
        "permissions-language-menu-button" |
        "permissions-menu" |
        "permissions-menu-basic" |
        "permissions-menu-button" |
        "permissions-menu-close" |
        "permissions-menu-granular" |
        "permissions-menu-inherited-message" |
        "permissions-menu-reset-to-organization" |
        "permissions-menu-save" |
        "project-dashboard-activity-chart" |
        "project-dashboard-activity-list" |
        "project-dashboard-base-word-count" |
        "project-dashboard-description" |
        "project-dashboard-key-count" |
        "project-dashboard-language-bar" |
        "project-dashboard-language-count" |
        "project-dashboard-language-label-keys" |
        "project-dashboard-language-label-percentage" |
        "project-dashboard-language-label-state" |
        "project-dashboard-language-label-words" |
        "project-dashboard-language-menu" |
        "project-dashboard-language-menu-export" |
        "project-dashboard-language-menu-settings" |
        "project-dashboard-members" |
        "project-dashboard-members-count" |
        "project-dashboard-progress" |
        "project-dashboard-project-totals" |
        "project-dashboard-reviewed-percentage" |
        "project-dashboard-tags" |
        "project-dashboard-text" |
        "project-dashboard-translated-percentage" |
        "project-delete-button" |
        "project-leave-button" |
        "project-list-languages" |
        "project-list-languages-item" |
        "project-list-more-button" |
        "project-list-translations-button" |
        "project-member-item" |
        "project-member-revoke-button" |
        "project-menu-item" |
        "project-menu-item-api-keys" |
        "project-menu-item-dashboard" |
        "project-menu-item-export" |
        "project-menu-item-import" |
        "project-menu-item-integrate" |
        "project-menu-item-languages" |
        "project-menu-item-members" |
        "project-menu-item-projects" |
        "project-menu-item-settings" |
        "project-menu-item-translations" |
        "project-menu-items" |
        "project-name-field" |
        "project-settings" |
        "project-settings-button" |
        "project-settings-delete-button" |
        "project-settings-description" |
        "project-settings-languages" |
        "project-settings-languages-list-edit-button" |
        "project-settings-languages-list-name" |
        "project-settings-name" |
        "project-settings-transfer-button" |
        "project-states-bar-bar" |
        "project-states-bar-dot" |
        "project-states-bar-legend" |
        "project-states-bar-root" |
        "project-states-bar-state-progress" |
        "project-transfer-autocomplete-field" |
        "project-transfer-autocomplete-suggested-option" |
        "project-transfer-confirmation-field" |
        "project-transfer-dialog" |
        "regenerate-pat-dialog-content" |
        "regenerate-pat-dialog-title" |
        "screenshot-image" |
        "screenshot-thumbnail" |
        "screenshot-thumbnail-delete" |
        "search-select" |
        "search-select-item" |
        "search-select-new" |
        "search-select-search" |
        "sensitive-dialog-otp-input" |
        "sensitive-dialog-password-input" |
        "sensitive-protection-dialog" |
        "settings-menu-item" |
        "sign-up-submit-button" |
        "tag-autocomplete-input" |
        "tag-autocomplete-option" |
        "transfer-project-apply-button" |
        "translation-create-key-input" |
        "translation-create-namespace-input" |
        "translation-create-translation-input" |
        "translation-edit-delete-button" |
        "translation-edit-key-field" |
        "translation-edit-translation-field" |
        "translation-field-label" |
        "translation-history-item" |
        "translation-state-button" |
        "translation-tools-machine-translation-item" |
        "translation-tools-translation-memory-item" |
        "translations-add-button" |
        "translations-auto-translated-clear-button" |
        "translations-auto-translated-indicator" |
        "translations-cell-cancel-button" |
        "translations-cell-close" |
        "translations-cell-comments-button" |
        "translations-cell-edit-button" |
        "translations-cell-save-button" |
        "translations-cell-screenshots-button" |
        "translations-cell-tab-comments" |
        "translations-cell-tab-edit" |
        "translations-cell-tab-history" |
        "translations-comments-input" |
        "translations-comments-load-more-button" |
        "translations-delete-button" |
        "translations-filter-clear-all" |
        "translations-filter-option" |
        "translations-filter-select" |
        "translations-filters-subheader" |
        "translations-history-load-more-button" |
        "translations-key-count" |
        "translations-language-select-form-control" |
        "translations-language-select-item" |
        "translations-namespace-banner" |
        "translations-outdated-clear-button" |
        "translations-outdated-indicator" |
        "translations-row" |
        "translations-row-checkbox" |
        "translations-select-all-button" |
        "translations-shortcuts-command" |
        "translations-state-indicator" |
        "translations-table-cell" |
        "translations-table-cell-language" |
        "translations-table-cell-translation" |
        "translations-tag" |
        "translations-tag-add" |
        "translations-tag-close" |
        "translations-tag-input" |
        "translations-tags-add" |
        "translations-toolbar-counter" |
        "translations-toolbar-to-top" |
        "translations-view-list" |
        "translations-view-list-button" |
        "translations-view-table" |
        "translations-view-table-button" |
        "user-delete-organization-message-item" |
        "user-menu-logout" |
        "user-menu-organization-settings" |
        "user-menu-organization-switch" |
        "user-menu-server-administration" |
        "user-menu-user-settings" |
        "user-profile"
}