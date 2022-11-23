package de.rki.coronawarnapp.submission

import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.errors.AlreadyRedeemedException
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.coroutine.AppScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubmissionRepository @Inject constructor(
    @AppScope private val scope: CoroutineScope,
    private val submissionSettings: SubmissionSettings,
    private val coronaTestRepository: CoronaTestRepository,
) {

    val pcrTest: Flow<PCRCoronaTest?> = coronaTestRepository.coronaTests.map { tests ->
        tests.singleOrNull { it.type == BaseCoronaTest.Type.PCR } as? PCRCoronaTest
    }

    val raTest: Flow<RACoronaTest?> = coronaTestRepository.coronaTests.map { tests ->
        tests.singleOrNull { it.type == BaseCoronaTest.Type.RAPID_ANTIGEN } as? RACoronaTest
    }

    fun testForType(type: BaseCoronaTest.Type) = when (type) {
        BaseCoronaTest.Type.PCR -> pcrTest
        BaseCoronaTest.Type.RAPID_ANTIGEN -> raTest
    }

    fun hasActiveTest(identifier: TestIdentifier, type: BaseCoronaTest.Type) =
        coronaTestRepository.allCoronaTests.map {
            val testToRestore = it.find { test -> test.identifier == identifier }
            if (testToRestore != null) { // Test exists in personal tests
                when (type) {
                    BaseCoronaTest.Type.PCR -> pcrTest.first()
                    BaseCoronaTest.Type.RAPID_ANTIGEN -> raTest.first()
                }
            } else {
                null
            }
        }

    suspend fun updateCurrentSymptoms(symptoms: Symptoms?) {
        submissionSettings.updateSymptoms(symptoms)
    }

    suspend fun giveConsentToSubmission(type: BaseCoronaTest.Type) {
        Timber.tag(TAG).v("giveConsentToSubmission(type=%s)", type)
        withContext(scope.coroutineContext) {
            val test = coronaTestRepository.coronaTests.first().singleOrNull { it.type == type }
                ?: throw IllegalStateException("No test of type $type available")
            Timber.tag(TAG).v("giveConsentToSubmission(type=$type): %s", test)
            coronaTestRepository.updateSubmissionConsent(identifier = test.identifier, consented = true)
        }
    }

    suspend fun revokeConsentToSubmission(type: BaseCoronaTest.Type) {
        Timber.tag(TAG).v("revokeConsentToSubmission(type=%s)", type)
        withContext(scope.coroutineContext) {
            val test = coronaTestRepository.coronaTests.first().singleOrNull { it.type == type }
                ?: throw IllegalStateException("No test of type $type available")

            coronaTestRepository.updateSubmissionConsent(identifier = test.identifier, consented = false)
        }
    }

    // to be set to true once the user has opened and viewed their test result
    suspend fun setViewedTestResult(type: BaseCoronaTest.Type) {
        Timber.tag(TAG).v("setViewedTestResult(type=%s)", type)
        withContext(scope.coroutineContext) {
            val test = coronaTestRepository.coronaTests.first().singleOrNull { it.type == type }
                ?: throw IllegalStateException("No test of type $type available")

            coronaTestRepository.markAsViewed(identifier = test.identifier)
        }
    }

    suspend fun refreshTest(type: BaseCoronaTest.Type? = null) {
        Timber.tag(TAG).v("refreshTest(type=%s)", type)

        withContext(scope.coroutineContext) {
            coronaTestRepository.refresh(type = type)
        }
    }

    suspend fun registerTest(request: TestRegistrationRequest): BaseCoronaTest {
        Timber.tag(TAG).v("registerTest(request=%s)", request)
        val coronaTest = coronaTestRepository.registerTest(request)
        Timber.d("Registered test %s -> %s", request, coronaTest)
        return coronaTest
    }

    /**
     * Attempt to register a new test, but if it is already redeemed, keep the previous test.
     */
    suspend fun tryReplaceTest(request: TestRegistrationRequest): BaseCoronaTest {
        Timber.tag(TAG).v("tryReplaceTest(request=%s)", request)

        val coronaTest = coronaTestRepository.registerTest(
            request = request,
            preCondition = { currentTests ->
                if (currentTests.any { it.type == request.type && it.isNotRecycled }) {
                    Timber.tag(TAG).i("Test type already exists, will try to replace.")
                }
                true
            },
            postCondition = { newTest ->
                if (newTest.isRedeemed) {
                    Timber.w("Replacement test was already redeemed, removing it, will not use.")
                    throw AlreadyRedeemedException(newTest)
                }
                true
            }
        )
        Timber.d("Registered test %s -> %s", request, coronaTest)
        return coronaTest
    }

    companion object {
        private const val TAG = "SubmissionRepository"
    }
}

fun CoronaTestResult?.toDeviceUIState(): DeviceUIState = when (this) {
    CoronaTestResult.PCR_NEGATIVE -> DeviceUIState.PAIRED_NEGATIVE
    CoronaTestResult.PCR_POSITIVE -> DeviceUIState.PAIRED_POSITIVE
    CoronaTestResult.PCR_OR_RAT_PENDING -> DeviceUIState.PAIRED_NO_RESULT
    CoronaTestResult.PCR_OR_RAT_REDEEMED -> DeviceUIState.PAIRED_REDEEMED
    CoronaTestResult.PCR_INVALID -> DeviceUIState.PAIRED_ERROR
    CoronaTestResult.RAT_PENDING -> DeviceUIState.PAIRED_NO_RESULT
    CoronaTestResult.RAT_NEGATIVE -> DeviceUIState.PAIRED_NEGATIVE
    CoronaTestResult.RAT_POSITIVE -> DeviceUIState.PAIRED_POSITIVE
    CoronaTestResult.RAT_REDEEMED -> DeviceUIState.PAIRED_REDEEMED
    CoronaTestResult.RAT_INVALID -> DeviceUIState.PAIRED_ERROR
    null -> DeviceUIState.UNPAIRED
}
