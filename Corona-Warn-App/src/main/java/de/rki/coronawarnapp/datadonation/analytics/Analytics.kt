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
//        val request: DonorModule.Request = ...
//        // Collect data from all donor modules
//        val contributions = donorModules.map { it.beginDonation(request) }
//        val ppaContainer: Any = ...
//
//        contributions.forEach {
//            it.injectData(ppaContainer)
//        }
//
//        val success = trySubmission(ppaContainer)
//        contributions.forEach {
//            it.finishDonation(success)
//        }
    }
}
