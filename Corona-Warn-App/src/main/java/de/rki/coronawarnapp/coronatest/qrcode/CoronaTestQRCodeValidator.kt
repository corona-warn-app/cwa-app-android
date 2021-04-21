package de.rki.coronawarnapp.coronatest.qrcode

import dagger.Reusable
import timber.log.Timber
import javax.inject.Inject

@Reusable
class CoronaTestQrCodeValidator @Inject constructor(
    raExtractor: RapidAntigenQrCodeExtractor,
    pcrExtractor: PcrQrCodeExtractor
) {
    private val extractors = setOf(raExtractor, pcrExtractor)

    fun validate(rawString: String): CoronaTestQRCode {
        return findExtractor(rawString)
            ?.extract(rawString)
            ?.also { Timber.i("Extracted data from QR code is $it") }
            ?: throw InvalidQRCodeException()
    }

    private fun findExtractor(rawString: String): QrCodeExtractor<CoronaTestQRCode>? {
        return extractors.find { it.canHandle(rawString) }
    }
}

interface QrCodeExtractor<T> {
    fun canHandle(rawString: String): Boolean
    fun extract(rawString: String): T
}
