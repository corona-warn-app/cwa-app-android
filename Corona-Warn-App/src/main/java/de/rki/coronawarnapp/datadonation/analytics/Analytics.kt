package de.rki.coronawarnapp.datadonation.analytics

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.analytics.server.DataDonationAnalyticsServer
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.datadonation.analytics.storage.LastAnalyticsSubmissionLogger
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaDataRequestAndroid
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.TimeStamper
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

    private suspend fun trySubmission(ppaData: PpaData.PPADataAndroid): Boolean {
        try {
            val ppaAttestationRequest = PPADeviceAttestationRequest(
                ppaData = ppaData
            )

            Timber.d("Starting safety net device attestation")

            val attestation = deviceAttestation.attest(ppaAttestationRequest)

            attestation.requirePass(appConfigProvider.getAppConfig().analytics.safetyNetRequirements)

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
            return false
        }
    }

    suspend fun collectContributions(ppaDataBuilder: PpaData.PPADataAndroid.Builder): List<DonorModule.Contribution> {
        val request: DonorModule.Request = object : DonorModule.Request {}

        val contributions = donorModules.map {
            Timber.d("Beginning donation for module: %s", it::class.simpleName)
            it.beginDonation(request)
        }

        contributions.forEach {
            Timber.d("Injecting contribution: %s", it::class.simpleName)
            it.injectData(ppaDataBuilder)
        }

        return contributions
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    suspend fun submitAnalyticsData() {
        Timber.d("Starting analytics submission")

        val ppaDataBuilder = PpaData.PPADataAndroid.newBuilder()

        val contributions = collectContributions(ppaDataBuilder = ppaDataBuilder)

        val analyticsProto = ppaDataBuilder.build()

        val success = trySubmission(analyticsProto)

        contributions.forEach {
            Timber.d("Finishing contribution: %s", it::class.simpleName)
            it.finishDonation(success)
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
    suspend fun stopDueToNoAnalyticsConfig(): Boolean {
        return !appConfigProvider.getAppConfig().analytics.analyticsEnabled
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun stopDueToNoUserConsent(): Boolean {
        return !settings.analyticsEnabled.value
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    suspend fun stopDueToProbabilityToSubmit(): Boolean {
        val submitRoll = Random.nextDouble(0.0, 1.0)
        return submitRoll > appConfigProvider.getAppConfig().analytics.probabilityToSubmit
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
        return onboarding.plus(Hours.hours(ONBOARDING_DELAY_HOURS).toStandardDuration())
            .isAfter(timeStamper.nowUTC)
    }

    suspend fun submitIfWanted() = submissionLockoutMutex.withLock {
        Timber.d("checking analytics conditions")

        if (stopDueToNoAnalyticsConfig()) {
            Timber.w("Aborting Analytics submission due to noAnalyticsConfig")
            return
        }

        if (stopDueToNoUserConsent()) {
            Timber.w("Aborting Analytics submission due to noUserConsent")
            return
        }

        if (stopDueToProbabilityToSubmit()) {
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

        submitAnalyticsData()
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

    companion object {
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
