package de.rki.coronawarnapp.exception.reporting

import java.io.IOException

open class ReportedException(
    override val code: Int? = ErrorCodes.REPORTED_EXCEPTION_PROBLEM.code,
    message: String? = null,
    cause: Throwable? = null,
    override val resId: Int? = null
) : Exception(
    message, cause
), ReportedExceptionInterface

open class ReportedIOException(
    override val code: Int? = ErrorCodes.REPORTED_IO_EXCEPTION_PROBLEM.code,
    message: String? = null,
    cause: Throwable? = null,
    override val resId: Int? = null
) : IOException(
    message, cause
), ReportedExceptionInterface

interface ReportedExceptionInterface {
    val code: Int?
    val resId: Int?
}
