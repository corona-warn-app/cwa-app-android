package de.rki.coronawarnapp.coronatest.qrcode.rapid

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.qrcode.scanner.QrCodeExtractor
import de.rki.coronawarnapp.tag
import timber.log.Timber
import javax.inject.Inject

class RapidPcrQrCodeExtractor @Inject constructor() : RapidQrCodeExtractor() {

    override suspend fun canHandle(rawString: String): Boolean = rawString.startsWith(PREFIX)
        .also {
            Timber.tag(TAG).d("canHandle(rawString=%s)", rawString)
            Timber.tag(TAG).d("Starts with '%s'? %b", PREFIX, it)
        }

    override val loggingTag: String
        get() = TAG

    override fun String.removeQrCodePrefix(): String = removePrefix(PREFIX)

    override fun CleanPayload.toCoronaTestQRCode(rawString: String) = CoronaTestQRCode.RapidPCR(
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
        private val TAG = tag<RapidPcrQrCodeExtractor>()
        private const val PREFIX: String = "https://p.coronawarn.app?v=1#"
    }
}
