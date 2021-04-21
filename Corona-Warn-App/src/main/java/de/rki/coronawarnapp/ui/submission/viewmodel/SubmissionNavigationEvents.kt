package de.rki.coronawarnapp.ui.submission.viewmodel

import com.google.android.gms.common.api.ApiException
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.tan.CoronaTestTAN

sealed class SubmissionNavigationEvents {
    object NavigateToContact : SubmissionNavigationEvents()
    object NavigateToDispatcher : SubmissionNavigationEvents()
    object NavigateToQRCodeScan : SubmissionNavigationEvents()
    object NavigateToDataPrivacy : SubmissionNavigationEvents()

    object NavigateToSymptomIntroduction : SubmissionNavigationEvents()
    object NavigateToTAN : SubmissionNavigationEvents()
    object NavigateToConsent : SubmissionNavigationEvents()
    object NavigateToMainActivity : SubmissionNavigationEvents()
    object NavigateToResultPendingScreen : SubmissionNavigationEvents()
    object NavigateToResultAvailableScreen : SubmissionNavigationEvents()
    data class NavigateToDeletionWarningFragmentFromQrCode(val coronaTestQRCode: CoronaTestQRCode, val consentGiven: Boolean) :
        SubmissionNavigationEvents()
    data class NavigateToDeletionWarningFragmentFromTan(val coronaTestTan: CoronaTestTAN, val consentGiven: Boolean) :
        SubmissionNavigationEvents()
    data class ResolvePlayServicesException(val exception: ApiException) : SubmissionNavigationEvents()
}
