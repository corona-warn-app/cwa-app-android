package de.rki.coronawarnapp.srs.core.model

sealed class SrsSubmissionResponse {
    object Success : SrsSubmissionResponse()
    data class TruncatedKeys(val days: String) : SrsSubmissionResponse()
}
