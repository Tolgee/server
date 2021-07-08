package io.tolgee.exceptions

import io.tolgee.constants.Message
import org.springframework.http.HttpStatus

class AuthenticationException(message: Message) : ErrorException(message) {

    override val httpStatus: HttpStatus
        get() = HttpStatus.UNAUTHORIZED
}
