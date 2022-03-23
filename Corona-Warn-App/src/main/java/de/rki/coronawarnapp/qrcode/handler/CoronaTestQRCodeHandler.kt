package de.rki.coronawarnapp.qrcode.handler

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsProvider
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import timber.log.Timber
import javax.inject.Inject

class CoronaTestQRCodeHandler @Inject constructor(
    private val recycledCoronaTestsProvider: RecycledCoronaTestsProvider
) {

    suspend fun handleQrCode(qrCode: CoronaTestQRCode): Result {
        Timber.tag(TAG).d("handleQrCode(qrCode=%s)", qrCode::class.java.simpleName)
        val recycledCoronaTest = recycledCoronaTestsProvider.findCoronaTest(qrCode.rawQrCode.toSHA256())
        return when {
            recycledCoronaTest != null -> InRecycleBin(recycledCoronaTest)
            else -> TestRegistrationSelection(coronaTestQrCode = qrCode)
        }.also { Timber.tag(TAG).d("returning %s", it::class.simpleName) }
    }

    sealed interface Result
    data class InRecycleBin(val recycledCoronaTest: CoronaTest) : Result
    data class TestRegistrationSelection(val coronaTestQrCode: CoronaTestQRCode) : Result
}

private val TAG = tag<CoronaTestQRCodeHandler>()
