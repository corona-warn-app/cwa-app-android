package de.rki.coronawarnapp.coronatest.qrcode

import dagger.Reusable
import de.rki.coronawarnapp.coronatest.type.CoronaTest.Type
import javax.inject.Inject

@Reusable
class CoronaTestQrCodeExtractor @Inject constructor() {

    fun extract(rawString: String): CoronaTestQRCode {
        return when (rawString.determineType()) {
            Type.PCR -> CoronaTestQRCode.PCR(rawString)
            Type.RAPID_ANTIGEN -> CoronaTestQRCode.RapidAntigen(rawString)
        }
    }

    private fun String.determineType(): Type {
        return when {
            startsWith(CoronaTestQRCode.PCR.prefix) -> Type.PCR
            startsWith(CoronaTestQRCode.RapidAntigen.prefix) -> Type.RAPID_ANTIGEN
            else -> throw InvalidQRCodeException()
        }
    }
}
