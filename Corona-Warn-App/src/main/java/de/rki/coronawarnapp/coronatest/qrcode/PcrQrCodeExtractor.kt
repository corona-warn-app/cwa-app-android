package de.rki.coronawarnapp.coronatest.qrcode

import de.rki.coronawarnapp.bugreporting.censors.submission.PcrQrCodeCensor
import java.util.regex.Pattern
import javax.inject.Inject

class PcrQrCodeExtractor @Inject constructor() : QrCodeExtractor<CoronaTestQRCode> {

    override fun canHandle(rawString: String): Boolean = rawString.startsWith(prefix, ignoreCase = true)

    override fun extract(rawString: String, mode: QrCodeExtractor.Mode): CoronaTestQRCode.PCR {
        val guid = extractGUID(rawString)
        PcrQrCodeCensor.lastGUID = guid
        return CoronaTestQRCode.PCR(guid)
    }

    private fun extractGUID(rawString: String): CoronaTestGUID {
        if (!pattern.toRegex().matches(rawString)) throw InvalidQRCodeException()

        val matcher = pattern.matcher(rawString)
        return if (matcher.matches()) {
            matcher.group(1) as CoronaTestGUID
        } else throw InvalidQRCodeException()
    }

    private val prefix: String = "https://localhost"

    private val pattern: Pattern = (
        "^" + // Match start of string
            "(?:https:\\/{2}localhost)" + // Match `https://localhost`
            "(?:\\/{1}\\?)" + // Match the query param `/?`
            "([a-f\\d]{6}[-][a-f\\d]{8}[-](?:[a-f\\d]{4}[-]){3}[a-f\\d]{12})" + // Match the UUID
            "\$"
        ).toPattern(Pattern.CASE_INSENSITIVE)
}
