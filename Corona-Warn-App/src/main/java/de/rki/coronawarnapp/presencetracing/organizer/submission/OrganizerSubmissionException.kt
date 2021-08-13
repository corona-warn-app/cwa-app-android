package de.rki.coronawarnapp.presencetracing.organizer.submission

// TODO Use Errors from Error handling ticket
class OrganizerSubmissionException(
    val errorCode: ErrorCode,
    override val cause: Throwable? = null
) : Exception(cause) {
    enum class ErrorCode {
        SUBMISSION_OB_CLIENT_ERROR,
        SUBMISSION_OB_SERVER_ERROR,
        SUBMISSION_OB_NO_NETWORK,
    }
}
