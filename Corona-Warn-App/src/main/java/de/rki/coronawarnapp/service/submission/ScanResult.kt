package de.rki.coronawarnapp.service.submission

import java.util.regex.Pattern

data class ScanResult(val rawResult: String) {
    val isValid by lazy {
        containsValidGUID(rawResult)
    }
    val guid: String? by lazy {
        if (isValid) guid
        else null
    }

    private fun matchGUIDPattern(scanResult: String): Boolean {
        val pattern = QR_CODE_REGEX
        return pattern.toRegex().matches(scanResult)
    }

    private fun containsValidGUID(rawResult: String): Boolean {
        if (rawResult.length > SubmissionConstants.MAX_QR_CODE_LENGTH ||
            rawResult.count { it == SubmissionConstants.GUID_SEPARATOR } != 1 ||
            !matchGUIDPattern(rawResult)
        )
            return false

        val potentialGUID = SubmissionService.extractGUID(rawResult)
        return !(potentialGUID.isEmpty() || potentialGUID.length > SubmissionConstants.MAX_GUID_LENGTH)
    }

    companion object {
        val QR_CODE_REGEX: Pattern = Pattern.compile("^((^https:\\/{2}localhost)(\\/\\?)[A-Fa-f0-9]{6}[-][A-Fa-f0-9]{8}" +
                "[-][A-Fa-f0-9]{4}[-][A-Fa-f0-9]{4}[-][A-Fa-f0-9]{4}[-][A-Fa-f0-9]{12})\$")
    }
}
