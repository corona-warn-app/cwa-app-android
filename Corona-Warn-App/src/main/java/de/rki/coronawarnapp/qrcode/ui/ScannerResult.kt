package de.rki.coronawarnapp.qrcode.ui

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.VerifiedTraceLocation
import de.rki.coronawarnapp.util.ui.LazyString

sealed interface ScannerResult

object InProgress : ScannerResult

sealed class DccResult : ScannerResult {
    data class Test(val containerId: TestCertificateContainerId) : DccResult()
    data class Vaccination(val containerId: VaccinationCertificateContainerId) : DccResult()
    data class Recovery(val containerId: RecoveryCertificateContainerId) : DccResult()
}

sealed class CheckInResult : ScannerResult {
    data class Details(val verifiedLocation: VerifiedTraceLocation) : CheckInResult()
    data class Error(val stringRes: LazyString) : CheckInResult()
}

sealed class CoronaTestResult : ScannerResult {
    data class DuplicateTest(val rawQrCode: String) : CoronaTestResult()
    data class ConsentTest(val rawQrCode: String) : CoronaTestResult()
}

data class Error(val error: Throwable) : ScannerResult
