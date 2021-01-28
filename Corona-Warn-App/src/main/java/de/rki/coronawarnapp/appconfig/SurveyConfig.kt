package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.mapping.ConfigMapper

interface SurveyConfig {
    val enableSurveyLinkOnHighRiskCard: Boolean
    val safetyNetRequirements: SafetyNetRequirements

    interface Mapper : ConfigMapper<SurveyConfig>
}
