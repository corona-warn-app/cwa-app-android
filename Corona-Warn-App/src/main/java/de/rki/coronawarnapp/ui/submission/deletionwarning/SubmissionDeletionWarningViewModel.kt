package de.rki.coronawarnapp.ui.submission.deletionwarning

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavDirections
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.NavGraphDirections
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.coronatest.tan.CoronaTestTAN
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.first
import timber.log.Timber

class SubmissionDeletionWarningViewModel @AssistedInject constructor(
    @Assisted private val coronaTestQrCode: CoronaTestQRCode?,
    @Assisted private val coronaTestQrTan: CoronaTestTAN?,
    @Assisted private val isConsentGiven: Boolean,
    private val submissionRepository: SubmissionRepository,
    private val coronaTestRepository: CoronaTestRepository,
) : CWAViewModel() {

    val routeToScreen = SingleLiveEvent<NavDirections>()
    private val mutableRegistrationState = MutableLiveData(RegistrationState())
    val registrationState: LiveData<RegistrationState> = mutableRegistrationState
    val registrationError = SingleLiveEvent<Throwable>()

    private fun getRegistrationType(): RegistrationType = if (coronaTestQrCode != null) {
        RegistrationType.QR
    } else {
        RegistrationType.TAN
    }

    // If there is no qrCode, it must be a TAN, and TANs are always PCR
    internal fun getTestType(): CoronaTest.Type = coronaTestQrCode?.type ?: CoronaTest.Type.PCR

    fun deleteExistingAndRegisterNewTest() = launch {
        when {
            coronaTestQrTan != null -> deleteExistingAndRegisterNewTestWitTAN()
            else -> deleteExistingAndRegisterNewTestWithQrCode()
        }
    }

    private suspend fun deleteExistingAndRegisterNewTestWithQrCode() = try {
        requireNotNull(coronaTestQrCode) { "QR Code was unavailable" }

        // Remove existing test and wait until that is done
        submissionRepository.testForType(coronaTestQrCode.type).first()?.let {
            coronaTestRepository.removeTest(it.identifier)
        } ?: Timber.w("Test we will replace with QR was already removed?")

        mutableRegistrationState.postValue(RegistrationState(isFetching = true))

        val coronaTest = submissionRepository.registerTest(coronaTestQrCode)

        if (coronaTest.isFinal) {
            Timber.d("New test was already final, removing it again: %s", coronaTest)
            // This does not wait until the test is removed,
            // the exception handling should navigate the user to a new screen anyways
            submissionRepository.removeTestFromDevice(type = coronaTest.type)

            throw InvalidQRCodeException()
        }

        if (isConsentGiven) {
            submissionRepository.giveConsentToSubmission(type = coronaTestQrCode.type)
        }

        continueWithNewTest(coronaTest)

        mutableRegistrationState.postValue(RegistrationState(coronaTest = coronaTest))
    } catch (e: Exception) {
        Timber.e(e, "Error during test registration via QR code")
        mutableRegistrationState.postValue(RegistrationState(isFetching = false))
        registrationError.postValue(e)
    }

    private suspend fun deleteExistingAndRegisterNewTestWitTAN() = try {
        requireNotNull(coronaTestQrTan) { "TAN was unavailable" }

        submissionRepository.testForType(CoronaTest.Type.PCR).first()?.let {
            coronaTestRepository.removeTest(it.identifier)
        } ?: Timber.w("Test we will replace with TAN was already removed?")

        mutableRegistrationState.postValue(RegistrationState(isFetching = true))

        val coronaTest = submissionRepository.registerTest(coronaTestQrTan)
        continueWithNewTest(coronaTest)

        mutableRegistrationState.postValue(RegistrationState(coronaTest = coronaTest))
    } catch (e: Exception) {
        Timber.e(e, "Error during test registration via TAN")
        mutableRegistrationState.postValue(RegistrationState(isFetching = false))
        registrationError.postValue(e)
    }

    fun onCancelButtonClick() {
        SubmissionDeletionWarningFragmentDirections
            .actionSubmissionDeletionWarningFragmentToSubmissionConsentFragment()
            .run { routeToScreen.postValue(this) }
    }

    private fun continueWithNewTest(coronaTest: CoronaTest) {
        Timber.d("Continuing with our new CoronaTest: %s", coronaTest)
        when (getRegistrationType()) {
            RegistrationType.QR -> {
                if (coronaTest.isPositive) {
                    SubmissionDeletionWarningFragmentDirections
                        .actionSubmissionDeletionWarningFragmentToSubmissionTestResultAvailableFragment(
                            testType = coronaTestQrCode!!.type
                        )
                        .run { routeToScreen.postValue(this) }
                } else {
                    // TODO check RAT Test
                    NavGraphDirections.actionSubmissionTestResultGreenCertificateFragment(
                        testType = coronaTestQrCode!!.type
                    ).run { routeToScreen.postValue(this) }
                }
            }
            RegistrationType.TAN -> {
                SubmissionDeletionWarningFragmentDirections
                    .actionSubmissionDeletionFragmentToSubmissionTestResultNoConsentFragment(getTestType())
                    .run { routeToScreen.postValue(this) }
            }
        }
    }

    data class RegistrationState(
        val isFetching: Boolean = false,
        val coronaTest: CoronaTest? = null,
    )

    sealed class RegistrationType {
        object TAN : RegistrationType()
        object QR : RegistrationType()
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SubmissionDeletionWarningViewModel> {
        fun create(
            coronaTestQrCode: CoronaTestQRCode?,
            coronaTestTan: CoronaTestTAN?,
            isConsentGiven: Boolean
        ): SubmissionDeletionWarningViewModel
    }
}
