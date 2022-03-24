package de.rki.coronawarnapp.qrcode.ui

import android.net.Uri
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.covidcertificate.common.qrcode.DccQrCode
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.dccticketing.core.allowlist.internal.DccTicketingAllowListException
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.qrcode.DccTicketingInvalidQrCodeException
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.VerifiedTraceLocation
import de.rki.coronawarnapp.reyclebin.coronatest.request.RestoreRecycledTestRequest
import de.rki.coronawarnapp.util.ui.LazyString

sealed interface ScannerResult

object InProgress : ScannerResult

object InfoScreen : ScannerResult

sealed class DccResult : ScannerResult {
    data class Details(val uri: Uri) : DccResult()
    data class Onboarding(val dccQrCode: DccQrCode) : DccResult()
    data class InRecycleBin(val recycledContainerId: CertificateContainerId) : DccResult()
    data class MaxPersonsWarning(val uri: Uri, val max: Int) : DccResult()
    data class MaxPersonsBlock(val max: Int) : DccResult()
}

sealed class CheckInResult : ScannerResult {
    data class Details(val verifiedLocation: VerifiedTraceLocation, val requireOnboarding: Boolean) : CheckInResult()
    data class Error(val lazyMessage: LazyString) : CheckInResult()
}

sealed class CoronaTestResult : ScannerResult {
    data class RestoreDuplicateTest(val restoreRecycledTestRequest: RestoreRecycledTestRequest) : CoronaTestResult()
    data class TestRegistrationSelection(val coronaTestQrCode: CoronaTestQRCode) : CoronaTestResult()
    data class InRecycleBin(val recycledCoronaTest: PersonalCoronaTest) : CoronaTestResult()
    data class TestPositive(val test: BaseCoronaTest) : CoronaTestResult()
    data class TestNegative(val test: BaseCoronaTest) : CoronaTestResult()
    data class TestInvalid(val test: BaseCoronaTest) : CoronaTestResult()
    data class TestPending(val test: BaseCoronaTest) : CoronaTestResult()
    data class WarnOthers(val test: BaseCoronaTest) : CoronaTestResult()
}

sealed class DccTicketingResult : ScannerResult {
    data class ConsentI(val transactionContext: DccTicketingTransactionContext) : DccTicketingResult()
}

data class Error(val error: Throwable) : ScannerResult {
    val isDccTicketingError = error is DccTicketingInvalidQrCodeException
    val isAllowListError = error is DccTicketingAllowListException
}

data class DccTicketingError(val error: DccTicketingException, val errorMsg: LazyString) : ScannerResult {
    val isDccTicketingMinVersionError
        get() = error.errorCode == DccTicketingException.ErrorCode.MIN_VERSION_REQUIRED
}
