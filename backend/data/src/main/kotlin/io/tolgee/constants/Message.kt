/*
 * Copyright (c) 2020. Tolgee
 */
package io.tolgee.constants

import java.util.*

enum class Message {
  API_KEY_NOT_FOUND,
  BAD_CREDENTIALS,
  CAN_NOT_REVOKE_OWN_PERMISSIONS,
  DATA_CORRUPTED,
  INVITATION_CODE_DOES_NOT_EXIST_OR_EXPIRED,
  LANGUAGE_TAG_EXISTS,
  LANGUAGE_NAME_EXISTS,
  LANGUAGE_NOT_FOUND,
  OPERATION_NOT_PERMITTED,
  REGISTRATIONS_NOT_ALLOWED,
  PROJECT_NOT_FOUND,
  RESOURCE_NOT_FOUND,
  SCOPE_NOT_FOUND,
  KEY_EXISTS,
  THIRD_PARTY_AUTH_ERROR_MESSAGE,
  THIRD_PARTY_AUTH_NO_EMAIL,
  THIRD_PARTY_AUTH_NO_SUB,
  THIRD_PARTY_AUTH_UNKNOWN_ERROR,
  THIRD_PARTY_UNAUTHORIZED,
  THIRD_PARTY_GOOGLE_WORKSPACE_MISMATCH,
  USERNAME_ALREADY_EXISTS,
  USERNAME_OR_PASSWORD_INVALID,
  USER_ALREADY_HAS_PERMISSIONS,
  USER_ALREADY_HAS_ROLE,
  USER_NOT_FOUND,
  FILE_NOT_IMAGE,
  FILE_TOO_BIG,
  INVALID_TIMESTAMP,
  EMAIL_NOT_VERIFIED,
  MISSING_CALLBACK_URL,
  INVALID_JWT_TOKEN,
  EXPIRED_JWT_TOKEN,
  GENERAL_JWT_ERROR,
  CANNOT_FIND_SUITABLE_ADDRESS_PART,
  ADDRESS_PART_NOT_UNIQUE,
  USER_IS_NOT_MEMBER_OF_ORGANIZATION,
  ORGANIZATION_HAS_NO_OTHER_OWNER,
  USER_HAS_NO_PROJECT_ACCESS,
  USER_IS_ORGANIZATION_OWNER,
  CANNOT_SET_LOWER_THAN_ORGANIZATION_BASE_PERMISSIONS,
  CANNOT_SET_YOUR_OWN_PERMISSIONS,
  USER_IS_ORGANIZATION_MEMBER,
  PROPERTY_NOT_MUTABLE,
  IMPORT_LANGUAGE_NOT_FROM_PROJECT,
  EXISTING_LANGUAGE_NOT_SELECTED,
  CONFLICT_IS_NOT_RESOLVED,
  LANGUAGE_ALREADY_SELECTED,
  CANNOT_PARSE_FILE,
  COULD_NOT_RESOLVE_PROPERTY,
  CANNOT_ADD_MORE_THEN_100_LANGUAGES,
  NO_LANGUAGES_PROVIDED,
  LANGUAGE_WITH_BASE_LANGUAGE_TAG_NOT_FOUND,
  LANGUAGE_NOT_FROM_PROJECT,
  CANNOT_DELETE_BASE_LANGUAGE,
  KEY_NOT_FROM_PROJECT,
  MAX_SCREENSHOTS_EXCEEDED,
  TRANSLATION_NOT_FROM_PROJECT,
  CAN_EDIT_ONLY_OWN_COMMENT,
  REQUEST_PARSE_ERROR,
  FILTER_BY_VALUE_STATE_NOT_VALID,
  IMPORT_HAS_EXPIRED,
  TAG_NOT_FROM_PROJECT,
  TRANSLATION_TEXT_TOO_LONG,
  INVALID_RECAPTCHA_TOKEN,
  CANNOT_LEAVE_OWNING_PROJECT,
  CANNOT_LEAVE_PROJECT_WITH_ORGANIZATION_ROLE,
  DONT_HAVE_DIRECT_PERMISSIONS,
  TAG_TOO_LOG,
  TOO_MANY_UPLOADED_IMAGES,
  ONE_OR_MORE_IMAGES_NOT_FOUND,
  SCREENSHOT_NOT_OF_KEY,
  SERVICE_NOT_FOUND,
  TOO_MANY_REQUESTS,
  TRANSLATION_NOT_FOUND,
  OUT_OF_CREDITS,
  KEY_NOT_FOUND,
  ORGANIZATION_NOT_FOUND,
  CANNOT_FIND_BASE_LANGUAGE,
  BASE_LANGUAGE_NOT_FOUND,
  NO_EXPORTED_RESULT,
  MULTIPLE_FILES_MUST_BE_ZIPPED,
  CANNOT_SET_YOUR_OWN_ROLE,
  ONLY_TRANSLATE_PERMISSION_ACCEPTS_LANGUAGES,
  OAUTH2_TOKEN_URL_NOT_SET,
  OAUTH2_USER_URL_NOT_SET,
  EMAIL_ALREADY_INVITED_OR_MEMBER;

  val code: String
    get() = name.lowercase(Locale.getDefault())
}
