package de.rki.coronawarnapp.datadonation.survey

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.datadonation.storage.OTPRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SurveyUrlProvider @Inject constructor(
    private val appConfigProvider: AppConfigProvider,
    private val otpRepo: OTPRepository
) {

    /**
     * Provides an Url for the Data Donation Surveys.
     *
     * @throws IllegalStateException If no one time password (otp) can be loaded from the OTPRepository of if the
     * AppConfig doesn't contain a link to a survey
     */
    suspend fun provideUrl(type: Surveys.Type): String {

        return when (type) {
            Surveys.Type.HIGH_RISK_ENCOUNTER -> {
                val surveyConfig = appConfigProvider.getAppConfig().survey

                val httpUrl = surveyConfig.surveyOnHighRiskUrl
                    ?: throw IllegalStateException("AppConfig doesn't contain a link to the high-risk card survey")

                val queryParameterNameOtp = surveyConfig.otpQueryParameterName

                val otp =
                    otpRepo.lastOTP?.uuid ?: throw IllegalStateException("Could not load OTP uuid from OTPRepository")

                httpUrl.newBuilder()
                    .addQueryParameter(queryParameterNameOtp, otp.toString())
                    .build()
                    .toUrl()
                    .toString()
            }
        }
    }
}
