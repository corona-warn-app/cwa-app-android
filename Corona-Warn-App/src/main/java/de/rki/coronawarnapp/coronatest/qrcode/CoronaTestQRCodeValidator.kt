package de.rki.coronawarnapp.coronatest.qrcode

import dagger.Reusable
import timber.log.Timber
import javax.inject.Inject

@Reusable
class CoronaTestQrCodeValidator @Inject constructor() {

    private val extractors = listOf(
        RapidAntigenQrCodeExtractor(),
        PcrQrCodeExtractor()
    )

    fun validate(rawString: String): CoronaTestQRCode {
        return extractors.find { it.canHandle(rawString) }
            ?.extract(rawString)?.also {
                Timber.i("Extracted data from QR code is $it")
            } ?: throw InvalidQRCodeException()
    }
}

internal interface QrCodeExtractor {
    fun canHandle(rawString: String): Boolean
    fun extract(rawString: String): CoronaTestQRCode
}
