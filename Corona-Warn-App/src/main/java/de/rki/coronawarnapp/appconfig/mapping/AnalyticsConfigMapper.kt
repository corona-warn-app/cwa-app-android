package de.rki.coronawarnapp.appconfig.mapping

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AnalyticsConfig
import de.rki.coronawarnapp.appconfig.SafetyNetRequirements
import de.rki.coronawarnapp.appconfig.SafetyNetRequirementsContainer
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid
import javax.inject.Inject

@Reusable
class AnalyticsConfigMapper @Inject constructor() : AnalyticsConfig.Mapper {
    override fun map(rawConfig: AppConfigAndroid.ApplicationConfigurationAndroid): AnalyticsConfig {
        // TODO
        return AnalyticsConfigContainer()
    }

    data class AnalyticsConfigContainer(
        override val safetyNetRequirements: SafetyNetRequirements = SafetyNetRequirementsContainer(),
        override val probabilityToSubmit: Float = 1f,
        override val probabilityToSubmitAfterRiskCalculation: Float = 1f,
        override val probabilityToSubmitNewExposureWindows: Float = 1f
    ) : AnalyticsConfig
}
