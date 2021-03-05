package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.mapping.ConfigMapper
import okhttp3.HttpUrl

interface SurveyConfig {
    /** The name of the query parameter to pass the OTP to the survey */
    val otpQueryParameterName: String

    /** True to enable the survey in case of a high risk card, false otherwise */
    val surveyOnHighRiskEnabled: Boolean

    /**
     * The url (without query parameters) to access the survey in case of a high risk
     * card
     */
    val surveyOnHighRiskUrl: HttpUrl?

    val safetyNetRequirements: SafetyNetRequirements

    interface Mapper : ConfigMapper<SurveyConfig>
}
