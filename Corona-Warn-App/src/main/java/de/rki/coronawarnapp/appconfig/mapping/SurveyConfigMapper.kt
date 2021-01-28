package de.rki.coronawarnapp.appconfig.mapping

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.SafetyNetRequirements
import de.rki.coronawarnapp.appconfig.SafetyNetRequirementsContainer
import de.rki.coronawarnapp.appconfig.SurveyConfig
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid
import javax.inject.Inject

@Reusable
class SurveyConfigMapper @Inject constructor() : SurveyConfig.Mapper {
    override fun map(rawConfig: AppConfigAndroid.ApplicationConfigurationAndroid): SurveyConfig {
        // TODO
        return SurveyConfigContainer()
    }

    data class SurveyConfigContainer(
        override val safetyNetRequirements: SafetyNetRequirements = SafetyNetRequirementsContainer(),
        override val enableSurveyLinkOnHighRiskCard: Boolean = true
    ) : SurveyConfig
}
