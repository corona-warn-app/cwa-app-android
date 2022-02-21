package de.rki.coronawarnapp.coronatest.qrcode.rapid

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.tag
import javax.inject.Inject

class RapidAntigenQrCodeExtractor @Inject constructor() : RapidQrCodeExtractor() {

    override suspend fun canHandle(rawString: String): Boolean {
        return rawString.startsWith(PREFIX1, ignoreCase = true) || rawString.startsWith(PREFIX2, ignoreCase = true)
    }

    override val loggingTag: String by lazy { tag<RapidAntigenQrCodeExtractor>() }

    override fun String.removeQrCodePrefix(): String = removePrefix(PREFIX1).removePrefix(PREFIX2)

    override fun CleanPayload.toCoronaTestQRCode(rawString: String) = CoronaTestQRCode.RapidAntigen(
        hash = hash,
        createdAt = createdAt,
        firstName = firstName,
        lastName = lastName,
        dateOfBirth = dateOfBirth,
        testId = testId,
        salt = salt,
        isDccSupportedByPoc = isDccSupportedByPoc,
        rawQrCode = rawString
    )

    companion object {
        private const val PREFIX1: String = "https://s.coronawarn.app?v=1#"
        private const val PREFIX2: String = "https://s.coronawarn.app/?v=1#"
    }
}
