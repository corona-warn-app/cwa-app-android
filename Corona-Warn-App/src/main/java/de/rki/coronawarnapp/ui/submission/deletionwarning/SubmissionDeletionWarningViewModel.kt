package de.rki.coronawarnapp.ui.submission.deletionwarning

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.tan.CoronaTestTAN
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsProvider
import de.rki.coronawarnapp.reyclebin.coronatest.request.RestoreRecycledTestRequest
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.TestRegistrationStateProcessor
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.first
import timber.log.Timber

class SubmissionDeletionWarningViewModel @AssistedInject constructor(
    @Assisted private val testRegistrationRequest: TestRegistrationRequest,
    private val registrationStateProcessor: TestRegistrationStateProcessor,
    private val recycledCoronaTestsProvider: RecycledCoronaTestsProvider,
    private val submissionRepository: SubmissionRepository,
) : CWAViewModel() {

    val routeToScreen = SingleLiveEvent<DuplicateWarningEvent>()
    val registrationState = registrationStateProcessor.state.asLiveData2()

    internal fun getTestType(): BaseCoronaTest.Type = testRegistrationRequest.type

    fun deleteExistingAndRegisterNewTest() = launch {
        removeAndRegisterNew(testRegistrationRequest)
    }

    private suspend fun removeAndRegisterNew(request: TestRegistrationRequest) {
        when (request) {
            is CoronaTestTAN -> {
                val newTest = registrationStateProcessor.startTestRegistration(
                    request = request,
                    isSubmissionConsentGiven = false,
                    allowTestReplacement = true
                )

                if (newTest == null) {
                    Timber.w("Test registration failed.")
                    return
                } else {
                    Timber.d("Continuing with our new CoronaTest: %s", newTest)
                }
            }

            is CoronaTestQRCode -> routeToScreen.postValue(
                DuplicateWarningEvent.Direction(
                    SubmissionDeletionWarningFragmentDirections
                        .actionSubmissionDeletionWarningFragmentToSubmissionConsentFragment(
                            request,
                            allowTestReplacement = true
                        )
                )
            )

            is RestoreRecycledTestRequest -> {
                val test = submissionRepository.hasActiveTest(request.identifier, request.type).first()
                if (test != null) {
                    recycledCoronaTestsProvider.recycleCoronaTest(test.identifier)
                    recycledCoronaTestsProvider.restoreCoronaTest(request.identifier)
                    if (request.openResult) restoreCertificates(test, request) else routeToScreen.postValue(
                        DuplicateWarningEvent.Back
                    )
                } else {
                    recycledCoronaTestsProvider.restoreCoronaTest(request.identifier)
                    val directions = if (request.openResult) {
                        DuplicateWarningEvent.Direction(
                            SubmissionDeletionWarningFragmentDirections
                                .actionSubmissionDeletionWarningFragmentToSubmissionTestResultPendingFragment(
                                    forceTestResultUpdate = true,
                                    testIdentifier = request.identifier
                                )
                        )
                    } else {
                        DuplicateWarningEvent.Back
                    }
                    routeToScreen.postValue(directions)
                }
            }
        }
    }

    private fun restoreCertificates(test: BaseCoronaTest, request: TestRegistrationRequest) {
        when {
            test.isPositive -> routeToScreen.postValue(
                DuplicateWarningEvent.Direction(
                    SubmissionDeletionWarningFragmentDirections
                        .actionSubmissionDeletionWarningFragmentToSubmissionTestResultKeysSharedFragment(
                            testIdentifier = request.identifier
                        )
                )
            )
            test.isNegative -> when (test.type) {
                BaseCoronaTest.Type.PCR -> routeToScreen.postValue(
                    DuplicateWarningEvent.Direction(
                        SubmissionDeletionWarningFragmentDirections
                            .actionSubmissionDeletionWarningFragmentToSubmissionTestResultNegativeFragment(
                                testIdentifier = request.identifier
                            )
                    )
                )
                BaseCoronaTest.Type.RAPID_ANTIGEN -> routeToScreen.postValue(
                    DuplicateWarningEvent.Direction(
                        SubmissionDeletionWarningFragmentDirections
                            .actionSubmissionDeletionWarningFragmentToSubmissionTestResultNegativeFragment(
                                testIdentifier = request.identifier
                            )
                    )
                )
            }
            test.isPending -> routeToScreen.postValue(
                DuplicateWarningEvent.Direction(
                    SubmissionDeletionWarningFragmentDirections
                        .actionSubmissionDeletionWarningFragmentToSubmissionTestResultPendingFragment(
                            testIdentifier = request.identifier
                        )
                )
            )
        }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SubmissionDeletionWarningViewModel> {
        fun create(
            testRegistrationRequest: TestRegistrationRequest
        ): SubmissionDeletionWarningViewModel
    }
}
