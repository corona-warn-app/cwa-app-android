package de.rki.coronawarnapp.exception

import de.rki.coronawarnapp.exception.reporting.ErrorCodes
import de.rki.coronawarnapp.exception.reporting.ReportedException

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
    ReportedException(ErrorCodes.DIAGNOSIS_KEY_SERVICE_PROBLEM.code, message, cause)
