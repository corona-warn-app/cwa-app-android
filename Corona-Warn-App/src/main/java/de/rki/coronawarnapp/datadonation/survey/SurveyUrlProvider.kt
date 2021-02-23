package de.rki.coronawarnapp.datadonation.survey

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import java.util.UUID
import javax.inject.Inject

@Reusable
class SurveyUrlProvider @Inject constructor(
    private val appConfigProvider: AppConfigProvider
) {

    /**
     * Provides Urls for Data Donation Surveys.
     *
     * @param type the type of the survey, e.g. HIGH_RISK_ENCOUNTER
     * @param otp an authenticated one time password
     *
     * @throws IllegalStateException If the AppConfig doesn't contain a link to a survey
     */
    suspend fun provideUrl(type: Surveys.Type, otp: UUID): String {

        return when (type) {
            Surveys.Type.HIGH_RISK_ENCOUNTER -> {
                val surveyConfig = appConfigProvider.getAppConfig().survey

                val httpUrl = surveyConfig.surveyOnHighRiskUrl
                    ?: throw IllegalStateException("AppConfig doesn't contain a link to the high-risk card survey")

                httpUrl.newBuilder()
                    .addQueryParameter(surveyConfig.otpQueryParameterName, otp.toString())
                    .build()
                    .toUrl()
                    .toString()
            }
        }
    }
}
