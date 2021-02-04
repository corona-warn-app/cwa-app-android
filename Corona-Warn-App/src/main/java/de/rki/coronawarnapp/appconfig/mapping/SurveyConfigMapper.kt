package de.rki.coronawarnapp.appconfig.mapping

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.SafetyNetRequirements
import de.rki.coronawarnapp.appconfig.SafetyNetRequirementsContainer
import de.rki.coronawarnapp.appconfig.SurveyConfig
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid
import de.rki.coronawarnapp.server.protocols.internal.v2.PpddEdusParameters
import org.joda.time.Duration
import timber.log.Timber
import javax.inject.Inject

@Reusable
class SurveyConfigMapper @Inject constructor() : SurveyConfig.Mapper {
    override fun map(rawConfig: AppConfigAndroid.ApplicationConfigurationAndroid): SurveyConfig {
        var otpQueryParameterName = ""
        var enableSurveyLinkOnHighRiskCard = false
        var surveyOnHighRiskUrl = ""
        var requireBasicIntegrity = false
        var requireCTSProfileMatch = false
        var requireEvaluationTypeBasic = false
        var requireEvaluationTypeHardwareBacked = false


        if (rawConfig.hasEventDrivenUserSurveyParameters()) {
            if (rawConfig.eventDrivenUserSurveyParameters.hasCommon()) {
                rawConfig.eventDrivenUserSurveyParameters.common.also {
                    otpQueryParameterName = it.otpQueryParameterName
                    enableSurveyLinkOnHighRiskCard = it.surveyOnHighRiskEnabled
                    surveyOnHighRiskUrl = it.surveyOnHighRiskUrl
                }
            }

            if (rawConfig.eventDrivenUserSurveyParameters.hasPpac()) {
                rawConfig.eventDrivenUserSurveyParameters.ppac.also {
                    requireBasicIntegrity = it.requireBasicIntegrity
                    requireCTSProfileMatch = it.requireCTSProfileMatch
                    requireEvaluationTypeBasic = it.requireEvaluationTypeBasic
                    requireEvaluationTypeHardwareBacked = it.requireEvaluationTypeHardwareBacked
                }
            }
        }

        return SurveyConfigContainer(
            otpQueryParameterName = otpQueryParameterName,
            surveyOnHighRiskEnabled = enableSurveyLinkOnHighRiskCard,
            surveyOnHighRiskUrl = surveyOnHighRiskUrl,
            safetyNetRequirements = SafetyNetRequirementsContainer(
                requireBasicIntegrity = requireBasicIntegrity,
                requireCTSProfileMatch = requireCTSProfileMatch,
                requireEvaluationTypeBasic = requireEvaluationTypeBasic,
                requireEvaluationTypeHardwareBacked = requireEvaluationTypeHardwareBacked
            )
        ).also { Timber.v("SurveyConfig=%s", it) }
    }

    data class SurveyConfigContainer(
        override val otpQueryParameterName: String,
        override val surveyOnHighRiskEnabled: Boolean,
        override val surveyOnHighRiskUrl: String,
        override val safetyNetRequirements: SafetyNetRequirements
    ) : SurveyConfig
}
