package de.rki.coronawarnapp.datadonation.analytics

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.appconfig.AnalyticsConfig
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.analytics.server.DataDonationAnalyticsServer
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.datadonation.analytics.storage.LastAnalyticsSubmissionLogger
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import de.rki.coronawarnapp.datadonation.safetynet.SafetyNetException
import de.rki.coronawarnapp.datadonation.safetynet.SafetyNetException.Type.ATTESTATION_REQUEST_FAILED
import de.rki.coronawarnapp.datadonation.safetynet.SafetyNetException.Type.INTERNAL_ERROR
import de.rki.coronawarnapp.playbook.Playbook
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaDataRequestAndroid
import de.rki.coronawarnapp.storage.OnboardingSettings
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.reset.Resettable
import de.rki.coronawarnapp.util.security.RandomStrong
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
@Suppress("LongParameterList")
class Analytics @Inject constructor(
    private val dataDonationAnalyticsServer: DataDonationAnalyticsServer,
    private val appConfigProvider: AppConfigProvider,
    private val deviceAttestation: DeviceAttestation,
    // @JvmSuppressWildcards is needed for @IntoSet injection in Kotlin
    private val donorModules: Set<@JvmSuppressWildcards DonorModule>,
    private val settings: AnalyticsSettings,
    private val logger: LastAnalyticsSubmissionLogger,
    private val timeStamper: TimeStamper,
    private val onboardingSettings: OnboardingSettings,
    @RandomStrong private val randomSource: Random,
    private val playbook: Playbook,
) : Resettable {
    private val submissionLockoutMutex = Mutex()

    private suspend fun trySubmission(analyticsConfig: AnalyticsConfig, ppaData: PpaData.PPADataAndroid): Result {
        return try {
            val ppaAttestationRequest = PPADeviceAttestationRequest(
                ppaData = ppaData
            )

            Timber.tag(TAG).d("Starting safety net device attestation")

            val attestation = deviceAttestation.attest(ppaAttestationRequest)

            attestation.requirePass(analyticsConfig.safetyNetRequirements)

            Timber.tag(TAG).d("Safety net attestation passed")

            val ppaContainer = PpaDataRequestAndroid.PPADataRequestAndroid.newBuilder()
                .setAuthentication(attestation.accessControlProtoBuf)
                .setPayload(ppaData)
                .build()

            Timber.tag(TAG).d("Starting analytics upload")

            dataDonationAnalyticsServer.uploadAnalyticsData(ppaContainer)

            Timber.tag(TAG).d("Analytics upload finished")

            Result(successful = true)
        } catch (exception: Exception) {
            exception.reportProblem(tag = TAG, info = "An error occurred during analytics submission")
            val retry = exception is SafetyNetException &&
                exception.type in listOf(ATTESTATION_REQUEST_FAILED, INTERNAL_ERROR)
            Result(successful = false, shouldRetry = retry)
        }
    }

    suspend fun collectContributions(
        configData: ConfigData,
        ppaDataBuilder: PpaData.PPADataAndroid.Builder
    ): List<DonorModule.Contribution> {
        val request: DonorModule.Request = object : DonorModule.Request {
            override val currentConfig: ConfigData = configData
        }

        val contributions = donorModules.mapNotNull {
            val moduleName = it::class.simpleName
            Timber.tag(TAG).d("Beginning donation for module: %s", moduleName)
            try {
                it.beginDonation(request)
            } catch (e: Exception) {
                e.reportProblem(TAG, "beginDonation($request): $moduleName failed")
                null
            }
        }

        contributions.forEach {
            val moduleName = it::class.simpleName
            Timber.tag(TAG).d("Injecting contribution: %s", moduleName)
            try {
                it.injectData(ppaDataBuilder)
            } catch (e: Exception) {
                e.reportProblem(TAG, "injectData($ppaDataBuilder): $moduleName failed")
            }
        }

        return contributions
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    suspend fun submitAnalyticsData(configData: ConfigData): Result {
        Timber.tag(TAG).d("Starting analytics submission")

        val ppaDataBuilder = PpaData.PPADataAndroid.newBuilder()

        val contributions = collectContributions(configData, ppaDataBuilder)

        val analyticsProto = ppaDataBuilder.build()

        val result = try {
            // 6min, if attestation and/or submission takes longer than that,
            // then we want to give modules still time to cleanup and get into a consistent state.
            val analytics = withTimeout(360_000) {
                trySubmission(configData.analytics, analyticsProto)
            }
            tryFakeKeySubmission(configData)
            analytics
        } catch (e: TimeoutCancellationException) {
            Timber.tag(TAG).e(e, "trySubmission() timed out after 360s.")
            Result(successful = false, shouldRetry = true)
        }

        contributions.forEach {
            val moduleName = it::class.simpleName
            Timber.tag(TAG).d("Finishing contribution($result) for %s", moduleName)
            try {
                it.finishDonation(result.successful)
            } catch (e: Exception) {
                e.reportProblem(TAG, "finishDonation($result): $moduleName failed")
            }
        }

        if (result.successful) {
            settings.lastSubmittedTimestamp.update {
                timeStamper.nowUTC
            }

            logger.storeAnalyticsData(analyticsProto)
        }

        Timber.tag(TAG).d("Finished analytics submission result=%s", result)
        return result
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    suspend fun tryFakeKeySubmission(configData: ConfigData) = runCatching {
        val probability = configData
            .analytics
            .plausibleDeniabilityParameters
            .probabilityOfFakeKeySubmission

        val randomDouble = randomSource.nextDouble()
        Timber.tag(TAG).d("randomDouble=%s, probability=%s", randomDouble, probability)
        if (randomDouble <= probability) {
            Timber.tag(TAG).d("Send fake key submission")
            playbook.submitFake()
        } else {
            Timber.tag(TAG).d("Skip fake key submission")
        }
    }.onFailure {
        Timber.tag(TAG).d("tryFakeKeySubmission -> ${it.localizedMessage}")
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun stopDueToNoAnalyticsConfig(analyticsConfig: AnalyticsConfig): Boolean {
        return !analyticsConfig.analyticsEnabled
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun stopDueToNoUserConsent(): Boolean {
        return !settings.analyticsEnabled.value
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun stopDueToProbabilityToSubmit(analyticsConfig: AnalyticsConfig): Boolean {
        val submitRoll = Random.nextDouble(0.0, 1.0)
        return submitRoll > analyticsConfig.probabilityToSubmit
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun stopDueToLastSubmittedTimestamp(): Boolean {
        val lastSubmit = settings.lastSubmittedTimestamp.value ?: return false
        return lastSubmit.plus(Duration.ofHours(LAST_SUBMISSION_MIN_AGE_HOURS))
            .isAfter(timeStamper.nowUTC)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    suspend fun stopDueToTimeSinceOnboarding(): Boolean {
        val onboarding = onboardingSettings.onboardingCompletedTimestamp.first() ?: return true
        return onboarding.plus(Duration.ofHours(ONBOARDING_DELAY_HOURS)).isAfter(timeStamper.nowUTC)
    }

    suspend fun submitIfWanted(): Result = submissionLockoutMutex.withLock {
        Timber.tag(TAG).d("Checking analytics conditions")
        val configData: ConfigData = appConfigProvider.getAppConfig()

        if (stopDueToNoAnalyticsConfig(configData.analytics)) {
            Timber.tag(TAG).w("Aborting Analytics submission due to noAnalyticsConfig")
            return@withLock Result(successful = false)
        }

        if (stopDueToNoUserConsent()) {
            Timber.tag(TAG).w("Aborting Analytics submission due to noUserConsent")
            return@withLock Result(successful = false)
        }

        if (stopDueToProbabilityToSubmit(configData.analytics)) {
            Timber.tag(TAG).w("Aborting Analytics submission due to probabilityToSubmit")
            return@withLock Result(successful = false)
        }

        if (stopDueToLastSubmittedTimestamp()) {
            Timber.tag(TAG).w("Aborting Analytics submission due to lastSubmittedTimestamp")
            return@withLock Result(successful = false)
        }

        if (stopDueToTimeSinceOnboarding()) {
            Timber.tag(TAG).w("Aborting Analytics submission due to timeSinceOnboarding")
            return@withLock Result(successful = false)
        }

        return@withLock submitAnalyticsData(configData)
    }

    private suspend fun deleteAllData() = submissionLockoutMutex.withLock {
        donorModules.forEach {
            it.deleteData()
        }
    }

    suspend fun setAnalyticsEnabled(enabled: Boolean) {
        settings.analyticsEnabled.update {
            enabled
        }

        if (!enabled) {
            deleteAllData()
        }
    }

    override suspend fun reset() {
        Timber.tag(TAG).d("reset()")
        setAnalyticsEnabled(false)
    }

    fun isAnalyticsEnabledFlow(): Flow<Boolean> =
        settings.analyticsEnabled.flow

    data class Result(
        val successful: Boolean,
        val shouldRetry: Boolean = false
    )

    companion object {
        private val TAG = Analytics::class.java.simpleName
        private const val LAST_SUBMISSION_MIN_AGE_HOURS = 23L
        private const val ONBOARDING_DELAY_HOURS = 24L

        data class PPADeviceAttestationRequest(
            val ppaData: PpaData.PPADataAndroid
        ) : DeviceAttestation.Request {
            override val scenarioPayload: ByteArray
                get() = ppaData.toByteArray()
        }
    }
}
