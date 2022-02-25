declare namespace DataCy {
    export type Value = 
        "add-box" |
        "api-keys-create-edit-dialog" |
        "api-keys-edit-button" |
        "api-keys-project-select-item" |
        "auto-avatar-img" |
        "avatar-image" |
        "avatar-menu-open-button" |
        "avatar-remove-button" |
        "avatar-upload-button" |
        "avatar-upload-file-input" |
        "base-language-select" |
        "comment" |
        "comment-menu" |
        "comment-menu-delete" |
        "comment-menu-needs-resolution" |
        "comment-resolve" |
        "comment-text" |
        "create-project-language-add-button" |
        "dashboard-projects-list-item" |
        "dropzone" |
        "dropzone-inner" |
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
        "global-list-item" |
        "global-list-item-text" |
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
        "integrate-navigation-title" |
        "integrate-select-api-key-step-content" |
        "integrate-select-api-key-step-label" |
        "integrate-weapon-selector-button" |
        "invite-generate-button" |
        "invite-generate-input-code" |
        "language-delete-button" |
        "language-modify-form" |
        "languages-create-autocomplete-field" |
        "languages-create-autocomplete-suggested-option" |
        "languages-create-cancel-prepared-button" |
        "languages-create-customize-button" |
        "languages-create-submit-button" |
        "languages-flag-selector-open-button" |
        "languages-modify-apply-button" |
        "languages-modify-cancel-button" |
        "languages-prepared-language-box" |
        "leave-organization-button" |
        "organization-address-part-field" |
        "organization-delete-button" |
        "organization-description-field" |
        "organization-invitation-cancel-button" |
        "organization-invitation-generate-button" |
        "organization-invitation-role-select" |
        "organization-invitations-generated-field" |
        "organization-members-leave-button" |
        "organization-members-remove-user-button" |
        "organization-name-field" |
        "organization-profile" |
        "organization-role-menu" |
        "organization-role-menu-button" |
        "organization-role-select-item" |
        "organization-settings-button" |
        "organization-side-menu" |
        "organizations-user-email" |
        "organizations-user-name" |
        "permission-select-item" |
        "permissions-menu" |
        "permissions-menu-button" |
        "permissions-revoke-button" |
        "project-leave-button" |
        "project-list-languages" |
        "project-list-languages-item" |
        "project-list-more-button" |
        "project-list-owner" |
        "project-menu-items" |
        "project-name-field" |
        "project-owner-select" |
        "project-owner-select-item" |
        "project-settings" |
        "project-settings-button" |
        "project-settings-languages-list-edit-button" |
        "project-settings-languages-list-item" |
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
        "screenshot-box" |
        "sign-up-submit-button" |
        "tag-autocomplete-input" |
        "tag-autocomplete-option" |
        "transfer-project-apply-button" |
        "translation-create-key-input" |
        "translation-create-translation-input" |
        "translation-edit-delete-button" |
        "translation-edit-key-field" |
        "translation-edit-translation-field" |
        "translation-field-label" |
        "translation-state-button" |
        "translations-add-button" |
        "translations-cell-cancel-button" |
        "translations-cell-close" |
        "translations-cell-comments-button" |
        "translations-cell-edit-button" |
        "translations-cell-save-button" |
        "translations-cell-screenshots-button" |
        "translations-cell-tab-comments" |
        "translations-cell-tab-edit" |
        "translations-comments-input" |
        "translations-comments-load-more-button" |
        "translations-delete-button" |
        "translations-filter-clear-all" |
        "translations-filter-option" |
        "translations-filter-select" |
        "translations-filters-subheader" |
        "translations-key-count" |
        "translations-language-select-form-control" |
        "translations-language-select-item" |
        "translations-row" |
        "translations-row-checkbox" |
        "translations-shortcuts-command" |
        "translations-state-indicator" |
        "translations-table-cell" |
        "translations-table-cell-language" |
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
        "user-account-side-menu" |
        "user-organizations-settings-subtitle-link" |
        "user-profile"
}