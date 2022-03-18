package de.rki.coronawarnapp.submission

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.censors.submission.PcrQrCodeCensor
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.errors.AlreadyRedeemedException
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.AnalyticsKeySubmissionCollector
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.exception.reporting.report
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class TestRegistrationStateProcessor @Inject constructor(
    private val submissionRepository: SubmissionRepository,
    private val analyticsKeySubmissionCollector: AnalyticsKeySubmissionCollector,
) {

    private val mutex = Mutex()

    sealed class State {
        object Idle : State()
        object Working : State()
        data class TestRegistered(val test: CoronaTest) : State()

        data class Error(val exception: Exception) : State() {
            fun getDialogBuilder(context: Context, comingFromTan: Boolean = false): MaterialAlertDialogBuilder {
                val builder = MaterialAlertDialogBuilder(context).apply {
                    setCancelable(true)
                }

                return when (exception) {
                    is AlreadyRedeemedException -> builder.apply {
                        if (comingFromTan) {
                            setTitle(R.string.submission_error_dialog_web_test_paired_title_tan)
                            setMessage(R.string.submission_error_dialog_web_test_paired_body_tan)
                        } else {
                            setTitle(R.string.submission_error_dialog_web_tan_redeemed_title)
                            setMessage(R.string.submission_error_dialog_web_tan_redeemed_body)
                        }
                        setPositiveButton(android.R.string.ok) { _, _ ->
                            /* dismiss */
                        }
                    }
                    is BadRequestException -> builder.apply {
                        if (comingFromTan) {
                            setTitle(R.string.submission_error_dialog_web_test_paired_title_tan)
                            setMessage(R.string.submission_error_dialog_web_test_paired_body_tan)
                        } else {
                            setTitle(R.string.submission_qr_code_scan_invalid_dialog_headline)
                            setMessage(R.string.submission_qr_code_scan_invalid_dialog_body)
                        }
                        setPositiveButton(android.R.string.ok) { _, _ ->
                            /* dismiss */
                        }
                    }
                    is CwaClientError, is CwaServerError -> builder.apply {
                        setTitle(R.string.submission_error_dialog_web_generic_error_title)
                        setMessage(R.string.submission_error_dialog_web_generic_network_error_body)
                        setPositiveButton(android.R.string.ok) { _, _ ->
                            /* dismiss */
                        }
                    }
                    is CwaWebException -> builder.apply {
                        setTitle(R.string.submission_error_dialog_web_generic_error_title)
                        setMessage(R.string.submission_error_dialog_web_generic_error_body)
                        setPositiveButton(android.R.string.ok) { _, _ ->
                            /* dismiss */
                        }
                    }
                    else -> exception.toErrorDialogBuilder(context)
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
    ): CoronaTest? = mutex.withLock {
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
        request: TestRegistrationRequest,
        personName: String,
        isSubmissionConsentGiven: Boolean
    ): CoronaTest? = mutex.withLock {
        return try {
            stateInternal.value = State.Working

            PcrQrCodeCensor.dateOfBirth = request.dateOfBirth
            // TODO val coronaTest =  registerTest(request)
            //  stateInternal.value = State.TestRegistered(test = coronaTest)
            null
        } catch (err: Exception) {
            stateInternal.value = State.Error(exception = err)
            if (err !is CwaWebException && err !is AlreadyRedeemedException) {
                err.report(ExceptionCategory.INTERNAL)
            }
            null
        }
    }
}
