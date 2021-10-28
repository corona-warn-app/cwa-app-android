package de.rki.coronawarnapp.qrcode.ui

import android.net.Uri
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.covidcertificate.common.qrcode.DccQrCode
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.VerifiedTraceLocation
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTest
import de.rki.coronawarnapp.reyclebin.coronatest.request.RestoreRecycledTestRequest
import de.rki.coronawarnapp.util.ui.LazyString

sealed interface ScannerResult

object InProgress : ScannerResult

sealed class DccResult : ScannerResult {
    data class Details(val uri: Uri) : DccResult()
    data class Onboarding(val dccQrCode: DccQrCode) : DccResult()
    data class InRecycleBin(val recycledContainerId: CertificateContainerId) : DccResult()
}

sealed class CheckInResult : ScannerResult {
    data class Details(val verifiedLocation: VerifiedTraceLocation, val requireOnboarding: Boolean) : CheckInResult()
    data class Error(val lazyMessage: LazyString) : CheckInResult()
}

sealed class CoronaTestResult : ScannerResult {
    data class DuplicateTest(val coronaTestQrCode: CoronaTestQRCode) : CoronaTestResult()
    data class RestoreDuplicateTest(val restoreRecycledTestRequest: RestoreRecycledTestRequest) : CoronaTestResult()
    data class PendingTestResult(val coronaTest: CoronaTest) : CoronaTestResult()
    data class ConsentTest(val coronaTestQrCode: CoronaTestQRCode) : CoronaTestResult()
    data class InRecycleBin(val recycledCoronaTest: RecycledCoronaTest) : CoronaTestResult()
    object Home : CoronaTestResult()
}

data class Error(val error: Throwable) : ScannerResult
