package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.mapping.ConfigMapper

interface AnalyticsConfig {

    val probabilityToSubmit: Float
    val probabilityToSubmitAfterRiskCalculation: Float
    val probabilityToSubmitNewExposureWindows: Float
    val safetyNetRequirements: SafetyNetRequirements

    interface Mapper : ConfigMapper<AnalyticsConfig>
}
