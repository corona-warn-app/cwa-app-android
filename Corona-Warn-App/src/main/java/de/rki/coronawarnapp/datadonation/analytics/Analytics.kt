package de.rki.coronawarnapp.datadonation.analytics

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.analytics.server.DataDonationAnalyticsServer
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaDataRequestAndroid
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Analytics @Inject constructor(
    private val dataDonationAnalyticsServer: DataDonationAnalyticsServer,
    private val appConfigProvider: AppConfigProvider,
    private val deviceAttestation: DeviceAttestation,
    private val donorModules: Set<DonorModule>,
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

    suspend fun submitAnalyticsData() {
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
    }
}
