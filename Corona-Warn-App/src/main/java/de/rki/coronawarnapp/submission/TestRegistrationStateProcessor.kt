package de.rki.coronawarnapp.submission

import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.censors.submission.PcrQrCodeCensor
import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.errors.AlreadyRedeemedException
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.familytest.core.repository.FamilyTestRepository
import de.rki.coronawarnapp.ui.dialog.displayDialog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class TestRegistrationStateProcessor @Inject constructor(
    private val submissionRepository: SubmissionRepository,
    private val familyTestRepository: FamilyTestRepository,
    private val analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector,
) {

    private val mutex = Mutex()

    sealed class State {
        object Idle : State()
        object Working : State()
        data class TestRegistered(val test: BaseCoronaTest) : State()

        data class Error(val exception: Exception) : State() {
            fun showExceptionDialog(
                fragment: Fragment,
                comingFromTan: Boolean = false,
                positiveButtonFunction: () -> Unit = { }
            ) {
                when (exception) {
                    is AlreadyRedeemedException -> fragment.displayDialog {
                        if (comingFromTan) {
                            title(R.string.submission_error_dialog_web_test_paired_title_tan)
                            message(R.string.submission_error_dialog_web_test_paired_body_tan)
                        } else {
                            title(R.string.submission_error_dialog_web_tan_redeemed_title)
                            message(R.string.submission_error_dialog_web_tan_redeemed_body)
                        }
                        positiveButton(android.R.string.ok) { positiveButtonFunction() }
                    }
                    is BadRequestException -> fragment.displayDialog {
                        if (comingFromTan) {
                            title(R.string.submission_error_dialog_web_test_paired_title_tan)
                            message(R.string.submission_error_dialog_web_test_paired_body_tan)
                        } else {
                            title(R.string.submission_qr_code_scan_invalid_dialog_headline)
                            message(R.string.submission_qr_code_scan_invalid_dialog_body)
                        }
                        positiveButton(android.R.string.ok) { positiveButtonFunction() }
                    }
                    is CwaClientError, is CwaServerError -> fragment.displayDialog {
                        title(R.string.submission_error_dialog_web_generic_error_title)
                        message(R.string.submission_error_dialog_web_generic_network_error_body)
                        positiveButton(android.R.string.ok) { positiveButtonFunction() }
                    }
                    is CwaWebException -> fragment.displayDialog {
                        title(R.string.submission_error_dialog_web_generic_error_title)
                        message(R.string.submission_error_dialog_web_generic_error_body)
                        positiveButton(android.R.string.ok) { positiveButtonFunction() }
                    }
                    else -> fragment.displayDialog { setError(exception) }
                }
            }
        }
    }

    private val stateInternal = MutableStateFlow<State>(State.Idle)
    val state: Flow<State> = stateInternal

    suspend fun startTestRegistration(
        request: TestRegistrationRequest,
        isSubmissionConsentGiven: Boolean,
        allowTestReplacement: Boolean,
    ): BaseCoronaTest? = mutex.withLock {
        return try {
            stateInternal.value = State.Working

            PcrQrCodeCensor.dateOfBirth = request.dateOfBirth
            val coronaTest = with(submissionRepository) {
                if (allowTestReplacement) tryReplaceTest(request) else registerTest(request)
            }

            if (isSubmissionConsentGiven) {
                submissionRepository.giveConsentToSubmission(type = coronaTest.type)
                if (request is CoronaTestQRCode) {
                    analyticsKeySubmissionCollector.reportAdvancedConsentGiven(request.type)
                }
            }

            stateInternal.value = State.TestRegistered(test = coronaTest)
            coronaTest
        } catch (err: Exception) {
            stateInternal.value = State.Error(exception = err)
            if (err !is CwaWebException && err !is AlreadyRedeemedException) {
                err.report(ExceptionCategory.INTERNAL)
            }
            null
        }
    }

    suspend fun startFamilyTestRegistration(
        request: CoronaTestQRCode,
        personName: String
    ): BaseCoronaTest? = mutex.withLock {
        return try {
            stateInternal.value = State.Working

            PcrQrCodeCensor.dateOfBirth = request.dateOfBirth
            val coronaTest = familyTestRepository.registerTest(request, personName)
            stateInternal.value = State.TestRegistered(test = coronaTest)
            coronaTest
        } catch (err: Exception) {
            stateInternal.value = State.Error(exception = err)
            if (err !is CwaWebException && err !is AlreadyRedeemedException) {
                err.report(ExceptionCategory.INTERNAL)
            }
            null
        }
    }
}
