package de.rki.coronawarnapp.exception.reporting

object ReportingConstants {
    const val ERROR_REPORT_LOCAL_BROADCAST_CHANNEL = "error-report"
    const val ERROR_REPORT_CATEGORY_EXTRA = "category"
    const val ERROR_REPORT_PREFIX_EXTRA = "prefix"
    const val ERROR_REPORT_SUFFIX_EXTRA = "suffix"
    const val ERROR_REPORT_MESSAGE_EXTRA = "message"
    const val ERROR_REPORT_STACK_EXTRA = "stack"
    const val ERROR_REPORT_CODE_EXTRA = "code"
    const val ERROR_REPORT_API_EXCEPTION_CODE = "api-exception-code"
    const val ERROR_REPORT_RES_ID = "res-id"
    val ERROR_REPORT_UNKNOWN_ERROR = ErrorCodes.REPORTED_EXCEPTION_UNKNOWN_PROBLEM.code

    const val STATUS_CODE_GOOGLE_UPDATE_NEEDED = 17
    const val STATUS_CODE_REACHED_REQUEST_LIMIT = 39508
    const val STATUS_CODE_GOOGLE_API_FAIL = 10
}
