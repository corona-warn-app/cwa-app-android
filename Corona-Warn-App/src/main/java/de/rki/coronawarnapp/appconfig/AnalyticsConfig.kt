package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.mapping.ConfigMapper

interface AnalyticsConfig {

    val safetyNetRequirements: SafetyNetRequirements
    val probabilityToSubmit: Double
    val hoursSinceTestRegistrationToSubmitTestResultMetadata: Int
    val hoursSinceTestResultToSubmitKeySubmissionMetadata: Int
    val probabilityToSubmitNewExposureWindows: Double

    interface Mapper : ConfigMapper<AnalyticsConfig>
}
