package de.rki.coronawarnapp.presencetracing.organizer.submission

// TODO Use Errors from Error handling ticket
class OrganizerSubmissionException(
    val errorCode: ErrorCode,
    override val cause: Throwable? = null
) : Exception(cause) {
    enum class ErrorCode {
        /** If the submission fails with HTTP status code `403` */
        SUBMISSION_OB_TAN_ERROR,

        /** If the submission fails with an HTTP status code `40x` */
        SUBMISSION_OB_CLIENT_ERROR,

        /** If the submission fails with an HTTP status code `50x` */
        SUBMISSION_OB_SERVER_ERROR,

        /** If the submission fails due to missing or poor network connection */
        SUBMISSION_OB_NO_NETWORK,
    }
}
