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

    private fun matchGUIDPattern(scanResult: String) =
        QR_CODE_REGEX.toRegex().matches(scanResult)

    private fun containsValidGUID(rawResult: String): Boolean {
        if (rawResult.length > MAX_QR_CODE_LENGTH ||
            rawResult.count { it == GUID_SEPARATOR } != 1 ||
            !matchGUIDPattern(rawResult)
        )
            return false

        val potentialGUID = extractGUID(rawResult)
        return !(potentialGUID.isEmpty() || potentialGUID.length > MAX_GUID_LENGTH)
    }

    fun extractGUID(scanResult: String): String =
        scanResult.substringAfterLast(GUID_SEPARATOR, "")

    companion object {
        // regex pattern for scanned QR code URL
        val QR_CODE_REGEX: Pattern = Pattern.compile("^((^https:\\/{2}localhost)(\\/\\?)[A-Fa-f0-9]{6}" +
                "[-][A-Fa-f0-9]{8}[-][A-Fa-f0-9]{4}[-][A-Fa-f0-9]{4}[-][A-Fa-f0-9]{4}[-][A-Fa-f0-9]{12})\$")
        const val GUID_SEPARATOR = '?'
        const val MAX_QR_CODE_LENGTH = 150
        const val MAX_GUID_LENGTH = 80
    }
}
