package de.rki.coronawarnapp.ui.submission.deletionwarning

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.tan.CoronaTestTAN
import de.rki.coronawarnapp.coronatest.type.CoronaTest
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

    internal fun getTestType(): CoronaTest.Type = testRegistrationRequest.type

    fun deleteExistingAndRegisterNewTest() = launch {
        removeAndRegisterNew(testRegistrationRequest)
    }

    private suspend fun removeAndRegisterNew(request: TestRegistrationRequest) {
        when (request) {
            is CoronaTestTAN -> {
                val newTest = registrationStateProcessor.startRegistration(
                    request = request,
                    isSubmissionConsentGiven = false,
                    allowReplacement = true
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
                val test = submissionRepository.testForType(request.type).first()
                if (test != null) {
                    recycledCoronaTestsProvider.recycleCoronaTest(test.identifier)
                    recycledCoronaTestsProvider.restoreCoronaTest(request.identifier)
                    when {
                        test.isPositive -> routeToScreen.postValue(
                            DuplicateWarningEvent.Direction(
                                SubmissionDeletionWarningFragmentDirections
                                    .actionSubmissionDeletionWarningFragmentToSubmissionTestResultKeysSharedFragment(
                                        testType = request.type,
                                        testIdentifier = request.identifier
                                    )
                            )
                        )
                        test.isNegative -> when (test.type) {
                            CoronaTest.Type.PCR -> routeToScreen.postValue(
                                DuplicateWarningEvent.Direction(
                                    SubmissionDeletionWarningFragmentDirections
                                        .actionSubmissionDeletionWarningFragmentToSubmissionTestResultNegativeFragment(
                                            testType = request.type,
                                            testIdentifier = request.identifier
                                        )
                                )
                            )
                            CoronaTest.Type.RAPID_ANTIGEN -> routeToScreen.postValue(
                                DuplicateWarningEvent.Direction(
                                    SubmissionDeletionWarningFragmentDirections
                                        .actionSubmissionDeletionWarningFragmentToNegativeRatFragment(
                                            testIdentifier = request.identifier
                                        )
                                )
                            )
                        }
                        test.isPending -> routeToScreen.postValue(
                            DuplicateWarningEvent.Direction(
                                SubmissionDeletionWarningFragmentDirections
                                    .actionSubmissionDeletionWarningFragmentToSubmissionTestResultPendingFragment(
                                        testType = request.type,
                                        testIdentifier = request.identifier
                                    )
                            )
                        )
                    }
                } else {
                    recycledCoronaTestsProvider.restoreCoronaTest(request.identifier)
                    val directions = if (request.fromRecycleBin) {
                        DuplicateWarningEvent.Back
                    } else {
                        DuplicateWarningEvent.Direction(
                            SubmissionDeletionWarningFragmentDirections
                                .actionSubmissionDeletionWarningFragmentToSubmissionTestResultPendingFragment(
                                    testType = request.type,
                                    forceTestResultUpdate = true,
                                    testIdentifier = request.identifier
                                )
                        )
                    }
                    routeToScreen.postValue(directions)
                }
            }
        }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SubmissionDeletionWarningViewModel> {
        fun create(
            testRegistrationRequest: TestRegistrationRequest
        ): SubmissionDeletionWarningViewModel
    }
}
