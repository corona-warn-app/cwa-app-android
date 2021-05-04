package de.rki.coronawarnapp.submission

import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.errors.CoronaTestNotFoundException
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryStorage
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("LongParameterList")
@Singleton
class SubmissionRepository @Inject constructor(
    @AppScope private val scope: CoroutineScope,
    private val submissionSettings: SubmissionSettings,
    private val tekHistoryStorage: TEKHistoryStorage,
    private val coronaTestRepository: CoronaTestRepository,
) {

    val pcrTest: Flow<PCRCoronaTest?> = coronaTestRepository.coronaTests.map { tests ->
        tests.singleOrNull { it.type == CoronaTest.Type.PCR } as? PCRCoronaTest
    }

    val raTest: Flow<RACoronaTest?> = coronaTestRepository.coronaTests.map { tests ->
        tests.singleOrNull { it.type == CoronaTest.Type.RAPID_ANTIGEN } as? RACoronaTest
    }

    fun testForType(type: CoronaTest.Type) = when (type) {
        CoronaTest.Type.PCR -> pcrTest
        CoronaTest.Type.RAPID_ANTIGEN -> raTest
    }

    val currentSymptoms = submissionSettings.symptoms

    // to be used by new submission flow screens
    fun giveConsentToSubmission(type: CoronaTest.Type) {
        Timber.tag(TAG).v("giveConsentToSubmission(type=%s)", type)
        scope.launch {
            val test = coronaTestRepository.coronaTests.first().singleOrNull { it.type == type }
                ?: throw IllegalStateException("No test of type $type available")
            Timber.tag(TAG).v("giveConsentToSubmission(type=$type): %s", test)
            coronaTestRepository.updateConsent(identifier = test.identifier, consented = true)
        }
    }

    // to be used by new submission flow screens
    fun revokeConsentToSubmission(type: CoronaTest.Type) {
        Timber.tag(TAG).v("revokeConsentToSubmission(type=%s)", type)
        scope.launch {
            val test = coronaTestRepository.coronaTests.first().singleOrNull { it.type == type }
                ?: throw IllegalStateException("No test of type $type available")

            coronaTestRepository.updateConsent(identifier = test.identifier, consented = false)
        }
    }

    // to be set to true once the user has opened and viewed their test result
    fun setViewedTestResult(type: CoronaTest.Type) {
        Timber.tag(TAG).v("setViewedTestResult(type=%s)", type)
        scope.launch {
            val test = coronaTestRepository.coronaTests.first().singleOrNull { it.type == type }
                ?: throw IllegalStateException("No test of type $type available")

            coronaTestRepository.markAsViewed(identifier = test.identifier)
        }
    }

    fun refreshTest(type: CoronaTest.Type? = null) {
        Timber.tag(TAG).v("refreshTest(type=%s)", type)

        scope.launch {
            coronaTestRepository.refresh(type = type)
        }
    }

    suspend fun registerTest(request: TestRegistrationRequest): CoronaTest {
        Timber.tag(TAG).v("registerTest(request=%s)", request)
        val coronaTest = coronaTestRepository.registerTest(request)
        Timber.d("Registered test %s -> %s", request, coronaTest)
        return coronaTest
    }

    suspend fun reset() {
        Timber.tag(TAG).v("reset()")
        tekHistoryStorage.clear()
        submissionSettings.clear()
    }

    fun removeTestFromDevice(type: CoronaTest.Type) {
        Timber.tag(TAG).v("removeTestFromDevice(type=%s)", type)

        scope.launch {
            val test = coronaTestRepository.coronaTests.first().singleOrNull { it.type == type }
            if (test == null) {
                Timber.tag(TAG).w("There is no test of type=$type to remove.")
                return@launch
            }
            try {
                coronaTestRepository.removeTest(identifier = test.identifier)
            } catch (e: CoronaTestNotFoundException) {
                Timber.tag(TAG).e(e, "Test not found (type=$type), already removed?")
            }
        }
    }

    companion object {
        private const val TAG = "SubmissionRepository"
    }
}

// TODO Temporary, mapping should be replaced with **[CoronaTest]**
fun CoronaTestResult?.toDeviceUIState(): DeviceUIState = when (this) {
    CoronaTestResult.PCR_NEGATIVE -> DeviceUIState.PAIRED_NEGATIVE
    CoronaTestResult.PCR_POSITIVE -> DeviceUIState.PAIRED_POSITIVE
    CoronaTestResult.PCR_OR_RAT_PENDING -> DeviceUIState.PAIRED_NO_RESULT
    CoronaTestResult.PCR_REDEEMED -> DeviceUIState.PAIRED_REDEEMED
    CoronaTestResult.PCR_INVALID -> DeviceUIState.PAIRED_ERROR
    CoronaTestResult.RAT_PENDING -> DeviceUIState.PAIRED_NO_RESULT
    CoronaTestResult.RAT_NEGATIVE -> DeviceUIState.PAIRED_NEGATIVE
    CoronaTestResult.RAT_POSITIVE -> DeviceUIState.PAIRED_POSITIVE
    CoronaTestResult.RAT_REDEEMED -> DeviceUIState.PAIRED_REDEEMED
    CoronaTestResult.RAT_INVALID -> DeviceUIState.PAIRED_ERROR
    null -> DeviceUIState.UNPAIRED
}
