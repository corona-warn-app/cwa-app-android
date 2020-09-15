package de.rki.coronawarnapp.service.submission

import java.util.regex.Pattern

data class QRScanResult(val rawResult: String) {

    val isValid: Boolean
        get() = guid != null
    val guid: String? by lazy { extractGUID(rawResult) }

    private fun extractGUID(rawResult: String): String? {
        if (rawResult.length > MAX_QR_CODE_LENGTH) return null
        if (rawResult.count { it == GUID_SEPARATOR } != 1) return null
        if (!QR_CODE_REGEX.toRegex().matches(rawResult)) return null

        val potentialGUID = rawResult.substringAfterLast(GUID_SEPARATOR, "")
        if (potentialGUID.isBlank() || potentialGUID.length > MAX_GUID_LENGTH) return null

        return potentialGUID
    }

    companion object {
        // regex pattern for scanned QR code URL
        val QR_CODE_REGEX: Pattern = Pattern.compile(
            "^((^https:\\/{2}localhost)(\\/\\?)[A-Fa-f0-9]{6}" +
                    "[-][A-Fa-f0-9]{8}[-][A-Fa-f0-9]{4}[-][A-Fa-f0-9]{4}[-][A-Fa-f0-9]{4}[-][A-Fa-f0-9]{12})\$"
        )
        const val GUID_SEPARATOR = '?'
        const val MAX_QR_CODE_LENGTH = 150
        const val MAX_GUID_LENGTH = 80
    }
}
