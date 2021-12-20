package de.rki.coronawarnapp.dccticketing.core.common

class DccTicketingJwtException(
    val errorCode: ErrorCode,
    override val cause: Throwable? = null
) : Exception(errorCode.message, cause) {

    enum class ErrorCode(val message: String) {
        JWT_VER_EMPTY_JWKS("JWT_VER_EMPTY_JWKS"),
        JWT_VER_ALG_NOT_SUPPORTED("JWT_VER_ALG_NOT_SUPPORTED"),
        JWT_VER_NO_KID("JWT_VER_NO_KID"),
        JWT_VER_NO_JWK_FOR_KID("JWT_VER_NO_JWK_FOR_KID"),
        JWT_VER_SIG_INVALID("JWT_VER_SIG_INVALID"),
    }
}
