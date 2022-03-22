package de.rki.coronawarnapp.qrcode.handler

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.qrcode.ui.CoronaTestResult
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsProvider
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import timber.log.Timber
import javax.inject.Inject

class CoronaTestQRCodeHandler @Inject constructor(
    private val recycledCoronaTestsProvider: RecycledCoronaTestsProvider
) {

    suspend fun handleQrCode(qrCode: CoronaTestQRCode): CoronaTestResult {
        Timber.tag(TAG).d("handleQrCode(qrCode=%s)", qrCode::class.java.simpleName)
        val recycledCoronaTest = recycledCoronaTestsProvider.findCoronaTest(qrCode.rawQrCode.toSHA256())
        return when {
            recycledCoronaTest != null -> CoronaTestResult.InRecycleBin(recycledCoronaTest)
            else -> CoronaTestResult.TestRegistrationSelection(qrCode)
        }.also { Timber.tag(TAG).d("returning %s", it::class.simpleName) }
    }
}

private val TAG = tag<CoronaTestQRCodeHandler>()
