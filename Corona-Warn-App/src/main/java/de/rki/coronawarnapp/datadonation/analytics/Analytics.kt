package de.rki.coronawarnapp.datadonation.analytics

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.appconfig.AnalyticsConfig
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.analytics.server.DataDonationAnalyticsServer
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.datadonation.analytics.storage.LastAnalyticsSubmissionLogger
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaDataRequestAndroid
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.joda.time.Hours
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class Analytics @Inject constructor(
    private val dataDonationAnalyticsServer: DataDonationAnalyticsServer,
    private val appConfigProvider: AppConfigProvider,
    private val deviceAttestation: DeviceAttestation,
    // @JvmSuppressWildcards is needed for @IntoSet injection in Kotlin
    private val donorModules: Set<@JvmSuppressWildcards DonorModule>,
    private val settings: AnalyticsSettings,
    private val logger: LastAnalyticsSubmissionLogger,
    private val timeStamper: TimeStamper
) {
    private val submissionLockoutMutex = Mutex()

    private suspend fun trySubmission(analyticsConfig: AnalyticsConfig, ppaData: PpaData.PPADataAndroid): Boolean {
        try {
            val ppaAttestationRequest = PPADeviceAttestationRequest(
                ppaData = ppaData
            )

            Timber.d("Starting safety net device attestation")

            val attestation = deviceAttestation.attest(ppaAttestationRequest)

            attestation.requirePass(analyticsConfig.safetyNetRequirements)

            Timber.d("Safety net attestation passed")

            val ppaContainer = PpaDataRequestAndroid.PPADataRequestAndroid.newBuilder()
                .setAuthentication(attestation.accessControlProtoBuf)
                .setPayload(ppaData)
                .build()

            Timber.d("Starting analytics upload")

            dataDonationAnalyticsServer.uploadAnalyticsData(ppaContainer)

            Timber.d("Analytics upload finished")

            return true
        } catch (err: Exception) {
            Timber.e(err, "Error during analytics submission")
            err.reportProblem(tag = TAG, info = "An error occurred during analytics submission")
            return false
        }
    }

    suspend fun collectContributions(ppaDataBuilder: PpaData.PPADataAndroid.Builder): List<DonorModule.Contribution> {
        val request: DonorModule.Request = object : DonorModule.Request {}

        val contributions = donorModules.mapNotNull {
            val moduleName = it::class.simpleName
            Timber.d("Beginning donation for module: %s", moduleName)
            try {
                it.beginDonation(request)
            } catch (e: Exception) {
                e.reportProblem(TAG, "beginDonation($request): $moduleName failed")
                null
            }
        }

        contributions.forEach {
            val moduleName = it::class.simpleName
            Timber.d("Injecting contribution: %s", moduleName)
            try {
                it.injectData(ppaDataBuilder)
            } catch (e: Exception) {
                e.reportProblem(TAG, "injectData($ppaDataBuilder): $moduleName failed")
            }
        }

        return contributions
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    suspend fun submitAnalyticsData(analyticsConfig: AnalyticsConfig) {
        Timber.d("Starting analytics submission")

        val ppaDataBuilder = PpaData.PPADataAndroid.newBuilder()

        val contributions = collectContributions(ppaDataBuilder = ppaDataBuilder)

        val analyticsProto = ppaDataBuilder.build()

        val success = trySubmission(analyticsConfig, analyticsProto)

        contributions.forEach {
            val moduleName = it::class.simpleName
            Timber.d("Finishing contribution: %s", moduleName)
            try {
                it.finishDonation(success)
            } catch (e: Exception) {
                e.reportProblem(TAG, "finishDonation($success): $moduleName failed")
            }
        }

        if (success) {
            settings.lastSubmittedTimestamp.update {
                timeStamper.nowUTC
            }

            logger.storeAnalyticsData(analyticsProto)
        }

        Timber.d("Finished analytics submission success=%s", success)
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
        return lastSubmit.plus(Hours.hours(LAST_SUBMISSION_MIN_AGE_HOURS).toStandardDuration())
            .isAfter(timeStamper.nowUTC)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun stopDueToTimeSinceOnboarding(): Boolean {
        val onboarding = LocalData.onboardingCompletedTimestamp()?.let { Instant.ofEpochMilli(it) } ?: return true
        return onboarding.plus(Hours.hours(ONBOARDING_DELAY_HOURS).toStandardDuration()).isAfter(timeStamper.nowUTC)
    }

    suspend fun submitIfWanted() = submissionLockoutMutex.withLock {
        Timber.d("Checking analytics conditions")
        val analyticsConfig: AnalyticsConfig = appConfigProvider.getAppConfig().analytics

        if (stopDueToNoAnalyticsConfig(analyticsConfig)) {
            Timber.w("Aborting Analytics submission due to noAnalyticsConfig")
            return
        }

        if (stopDueToNoUserConsent()) {
            Timber.w("Aborting Analytics submission due to noUserConsent")
            return
        }

        if (stopDueToProbabilityToSubmit(analyticsConfig)) {
            Timber.w("Aborting Analytics submission due to probabilityToSubmit")
            return
        }

        if (stopDueToLastSubmittedTimestamp()) {
            Timber.w("Aborting Analytics submission due to lastSubmittedTimestamp")
            return
        }

        if (stopDueToTimeSinceOnboarding()) {
            Timber.w("Aborting Analytics submission due to timeSinceOnboarding")
            return
        }

        submitAnalyticsData(analyticsConfig)
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

    fun isAnalyticsEnabledFlow(): Flow<Boolean> =
        settings.analyticsEnabled.flow

    companion object {
        private val TAG = Analytics::class.java.simpleName
        private const val LAST_SUBMISSION_MIN_AGE_HOURS = 23
        private const val ONBOARDING_DELAY_HOURS = 24

        data class PPADeviceAttestationRequest(
            val ppaData: PpaData.PPADataAndroid
        ) : DeviceAttestation.Request {
            override val scenarioPayload: ByteArray
                get() = ppaData.toByteArray()
        }
    }
}
