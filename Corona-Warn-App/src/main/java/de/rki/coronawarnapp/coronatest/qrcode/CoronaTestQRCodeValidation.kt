package de.rki.coronawarnapp.coronatest.qrcode

import dagger.Reusable
import javax.inject.Inject

@Reusable
class CoronaTestQRCodeValidation @Inject constructor() {

    suspend fun validate(qrCode: String): CoronaTestQRCode {
        TODO()
    }
}
