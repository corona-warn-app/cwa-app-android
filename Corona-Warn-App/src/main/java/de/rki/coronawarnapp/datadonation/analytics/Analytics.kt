package de.rki.coronawarnapp.datadonation.analytics

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.analytics.server.DataDonationAnalyticsServer
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaDataRequestAndroid
import de.rki.coronawarnapp.storage.LocalData
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
    private val donorModules: Set<@JvmSuppressWildcards DonorModule>,
    private val settings: AnalyticsSettings
) {
    val isEnabled: Boolean = true

    private suspend fun trySubmission(ppaData: PpaData.PPADataAndroid): Boolean {
        try {
            val attestation = deviceAttestation.attest(object : DeviceAttestation.Request {
                override val scenarioPayload: ByteArray
                    get() = ppaData.toByteArray()
            })

            val ppaContainer = PpaDataRequestAndroid.PPADataRequestAndroid.newBuilder()
                .setAuthentication(attestation.accessControlProtoBuf)
                .setPayload(ppaData)
                .build()

            dataDonationAnalyticsServer.uploadAnalyticsData(ppaContainer)

            return true
        } catch (err: Exception) {
            Timber.i(err, "Error during analytics submission")
            return false
        }
    }

    private suspend fun submitAnalyticsData() {
        Timber.d("Starting analytics submission")

        val request: DonorModule.Request = object : DonorModule.Request {}

        val contributions = donorModules.map { it.beginDonation(request) }

        val ppaDataBuilder = PpaData.PPADataAndroid.newBuilder()

        contributions.forEach {
            it.injectData(ppaDataBuilder)
        }

        val success = trySubmission(ppaDataBuilder.build())

        contributions.forEach {
            it.finishDonation(success)
        }

        settings.lastSubmittedTimestamp.update {
            Instant.now()
        }
    }

    private suspend fun stopDueToProbabilityToSubmit(): Boolean {
        val submitRoll = Random.nextDouble(0.0, 1.0)
        return submitRoll > appConfigProvider.getAppConfig().analytics.probabilityToSubmit
    }

    private fun stopDueToLastSubmittedTimestamp(): Boolean {
        val lastSubmit = settings.lastSubmittedTimestamp.value ?: return false
        return lastSubmit.plus(Hours.hours(LAST_SUBMISSION_MIN_AGE_HOURS).toStandardDuration()).isAfterNow
    }

    private fun stopDueToTimeSinceOnboarding(): Boolean {
        val onboarding = LocalData.onboardingCompletedTimestamp()?.let { Instant.ofEpochMilli(it) } ?: return true
        return onboarding.plus(Hours.hours(ONBOARDING_DELAY_HOURS).toStandardDuration()).isAfterNow
    }

    suspend fun submitIfWanted() {
        Timber.d("checking analytics conditions")

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

    companion object {
        private const val LAST_SUBMISSION_MIN_AGE_HOURS = 23
        private const val ONBOARDING_DELAY_HOURS = 24
    }
}
