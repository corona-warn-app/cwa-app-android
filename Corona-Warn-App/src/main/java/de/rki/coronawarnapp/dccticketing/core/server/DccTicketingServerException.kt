package de.rki.coronawarnapp.dccticketing.core.server

class DccTicketingServerException(
    val errorCode: ErrorCode,
    override val cause: Throwable? = null
) : Exception(errorCode.message, cause) {

    enum class ErrorCode(
        val message: String
    ) {
        PARSE_ERR(""),
        SERVER_ERR(""),
        CLIENT_ERR(""),
        NO_NETWORK("")
    }
}
