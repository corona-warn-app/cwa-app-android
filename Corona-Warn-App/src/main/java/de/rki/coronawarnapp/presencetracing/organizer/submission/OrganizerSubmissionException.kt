package de.rki.coronawarnapp.presencetracing.organizer.submission

class OrganizerSubmissionException(
    val errorCode: ErrorCode,
    override val cause: Throwable? = null
) : Exception(cause) {

    // TODO finalise error handling and display right messages
    enum class ErrorCode {
        SUBMISSION_OB_CLIENT_ERROR,
        SUBMISSION_OB_SERVER_ERROR,
        SUBMISSION_OB_NO_NETWORK,
    }
}
