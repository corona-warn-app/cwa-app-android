package de.rki.coronawarnapp.qrcode.ui

import android.net.Uri
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.VerifiedTraceLocation
import de.rki.coronawarnapp.util.ui.LazyString

sealed interface ScannerResult

object InProgress : ScannerResult

data class DccResult(val uri: Uri) : ScannerResult {
    enum class Type(name: String) {
        VACCINATION("VACCINATION"),
        RECOVERY("RECOVERY"),
        TEST("TEST");

        companion object {
            fun ofString(type: String?): Type? {
                return values().find { it.name == type }
            }
        }
    }
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
