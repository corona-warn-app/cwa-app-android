package de.rki.coronawarnapp.datadonation.analytics

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Analytics @Inject constructor(
    private val appConfigProvider: AppConfigProvider,
    private val deviceAttestation: DeviceAttestation,
    private val donorModules: Set<DonorModule>,
    private val settings: AnalyticsSettings
) {

    val isEnabled: Boolean = true

    suspend fun submitAnalyticsData() {
        val request: DonorModule.Request = TODO()
        // Collect data from all donor modules
        val contributions = donorModules.map { it.startDonation(request) }
        val ppaContainer: Any = TODO("protobuf")

        contributions.forEach {
            it.injectData(ppaContainer)
        }

        val success = trySubmission(ppaContainer)
        contributions.forEach {
            it.finishContribution(success)
        }
    }

    private fun trySubmission(ppaContainer: Any): Boolean {
        TODO()
    }
}
