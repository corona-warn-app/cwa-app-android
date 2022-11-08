package de.rki.coronawarnapp.ui.submission.viewmodel

import com.google.android.gms.common.api.ApiException
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.tan.CoronaTestTAN
import de.rki.coronawarnapp.submission.TestRegistrationStateProcessor

sealed class SubmissionNavigationEvents {
    object NavigateToContact : SubmissionNavigationEvents()

    object NavigateToDispatcher : SubmissionNavigationEvents()

    object NavigateToQRCodeScan : SubmissionNavigationEvents()

    object NavigateToDataPrivacy : SubmissionNavigationEvents()

    object NavigateToSymptomIntroduction : SubmissionNavigationEvents()

    object NavigateToTAN : SubmissionNavigationEvents()

    object NavigateToMainActivity : SubmissionNavigationEvents()

    data class RegisterTestResult(val state: TestRegistrationStateProcessor.State) : SubmissionNavigationEvents()

    data class NavigateToSelfTestConsentScreen(val positiveNoAnswer: Boolean = false) : SubmissionNavigationEvents()

    data class NavigateToRequestDccFragment(
        val coronaTestQRCode: CoronaTestQRCode,
        val consentGiven: Boolean,
        val allowReplacement: Boolean
    ) : SubmissionNavigationEvents()

    data class NavigateToDeletionWarningFragmentFromTan(val coronaTestTan: CoronaTestTAN, val consentGiven: Boolean) :
        SubmissionNavigationEvents()

    data class NavigateToProfileList(val onboarded: Boolean = false) : SubmissionNavigationEvents()

    data class ResolvePlayServicesException(val exception: ApiException) : SubmissionNavigationEvents()

    object OpenTestCenterUrl : SubmissionNavigationEvents()

    object NavigateBackToTestRegistration : SubmissionNavigationEvents()

    object NavigateClose : SubmissionNavigationEvents()
}
