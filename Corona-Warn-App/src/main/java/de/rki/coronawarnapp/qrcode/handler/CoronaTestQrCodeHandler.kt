package de.rki.coronawarnapp.qrcode.handler

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.TestRegistrationStateProcessor
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CoronaTestQrCodeHandler @Inject constructor(
    private val registrationStateProcessor: TestRegistrationStateProcessor,
    private val submissionRepository: SubmissionRepository
) {

    /**
     * @throws [InvalidQRCodeException]
     */
    suspend fun handleQrCode(qrcode: CoronaTestQRCode): SubmissionNavigationEvents {
        val coronaTest = submissionRepository.testForType(qrcode.type).first()
        return when {
            coronaTest != null -> SubmissionNavigationEvents.NavigateToDeletionWarningFragmentFromQrCode(
                coronaTestQRCode = qrcode,
                consentGiven = false
            )
            else -> if (!qrcode.isDccSupportedByPoc) {
                val state = registrationStateProcessor.registerCoronaTest(
                    request = qrcode,
                    isSubmissionConsentGiven = false,
                    allowReplacement = false
                )
                SubmissionNavigationEvents.RegisterTestResult(state)
            } else {
                SubmissionNavigationEvents.NavigateToRequestDccFragment(qrcode, false)
            }
        }
    }
}
