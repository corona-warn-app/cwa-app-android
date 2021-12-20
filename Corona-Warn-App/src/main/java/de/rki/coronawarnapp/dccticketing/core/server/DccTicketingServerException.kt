package de.rki.coronawarnapp.dccticketing.core.server

class DccTicketingServerException(
    val errorCode: ErrorCode,
    override val cause: Throwable? = null
) : Exception(errorCode.message, cause) {

    enum class ErrorCode(
        val message: String
    ) {
        PARSE_ERR("Response could not be parsed to the target data structure"),
        SERVER_ERR("Request failed with a server error"),
        CLIENT_ERR("Request failed with a client error"),
        NO_NETWORK("Request failed because of a missing or poor network connection")
    }
}
