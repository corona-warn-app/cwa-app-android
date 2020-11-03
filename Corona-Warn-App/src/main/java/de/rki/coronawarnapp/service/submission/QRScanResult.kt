package de.rki.coronawarnapp.service.submission

import java.util.regex.Pattern

data class QRScanResult(val rawResult: String) {

    val isValid: Boolean
        get() = guid != null
    val guid: String? by lazy { extractGUID(rawResult) }

    private fun extractGUID(rawResult: String): String? {
        if (!QR_CODE_REGEX.toRegex().matches(rawResult)) return null

        val matcher = QR_CODE_REGEX.matcher(rawResult)
        return if (matcher.matches()) matcher.group(1) else null
    }

    companion object {
        // regex pattern for scanned QR code URL
        val QR_CODE_REGEX: Pattern = ("^" + // Match start of string
            "(?:https:\\/{2}localhost)" + // Match `https://localhost`
            "(?:\\/{1}\\?)" + // Match the query param `/?`
            "([a-f\\d]{6}[-][a-f\\d]{8}[-](?:[a-f\\d]{4}[-]){3}[a-f\\d]{12})" + // Match the UUID
            "\$"
            ).toPattern(Pattern.CASE_INSENSITIVE)
    }
}
