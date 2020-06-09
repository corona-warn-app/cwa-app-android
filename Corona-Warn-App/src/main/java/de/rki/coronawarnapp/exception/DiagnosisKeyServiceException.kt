package de.rki.coronawarnapp.exception

/**
 * An Exception thrown when an error occurs inside the DiagnosisKeyService
 *
 * @param message an Exception thrown inside the DiagnosisKeyService
 * @param cause the cause of the error
 *
 * @see DiagnosisKeySubmissionException
 * @see DiagnosisKeyRetrievalException
 */
abstract class DiagnosisKeyServiceException(message: String, cause: Throwable) :
    Exception(message, cause)
