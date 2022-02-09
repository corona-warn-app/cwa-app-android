package de.rki.coronawarnapp.coronatest.qrcode

import de.rki.coronawarnapp.qrcode.scanner.QrCodeExtractor
import timber.log.Timber
import javax.inject.Inject

class RapidPcrQrCodeExtractor @Inject constructor() : QrCodeExtractor<CoronaTestQRCode> {
    override suspend fun canHandle(rawString: String): Boolean = rawString.startsWith(PREFIX)
        .also {
            Timber.d("canHandle(rawString=%s)", rawString)
            Timber.d("Starts with '%s'? %b", PREFIX, it)
        }

    override suspend fun extract(rawString: String): CoronaTestQRCode {
        TODO("Not yet implemented")
    }
}

private const val PREFIX: String = "https://p.coronawarn.app?v=1#"
