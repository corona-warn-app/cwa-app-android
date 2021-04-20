package de.rki.coronawarnapp.coronatest.qrcode

import dagger.Reusable
import timber.log.Timber
import javax.inject.Inject

@Reusable
class CoronaTestQrCodeValidator @Inject constructor(
    ratExtractor: RapidAntigenQrCodeExtractor,
    pcrExtractor: PcrQrCodeExtractor
) {

    private val extractors = setOf(ratExtractor, pcrExtractor)

    fun validate(rawString: String): CoronaTestQRCode {
        return findExtractor(rawString)
            ?.extract(rawString)
            ?.also { Timber.i("Extracted data from QR code is $it") }
            ?: throw InvalidQRCodeException()
    }

    fun canHandle(rawString: String): Boolean {
        return findExtractor(rawString) != null
    }

    private fun findExtractor(rawString: String): QrCodeExtractor? {
        return extractors
            .find { it.canHandle(rawString) }
    }
}

interface QrCodeExtractor {
    fun canHandle(rawString: String): Boolean
    fun extract(rawString: String): CoronaTestQRCode
}
