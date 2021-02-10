package de.rki.coronawarnapp.appconfig.mapping

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.AnalyticsConfig
import de.rki.coronawarnapp.appconfig.SafetyNetRequirements
import de.rki.coronawarnapp.appconfig.SafetyNetRequirementsContainer
import de.rki.coronawarnapp.appconfig.internal.ApplicationConfigurationInvalidException
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid
import javax.inject.Inject

@Reusable
class AnalyticsConfigMapper @Inject constructor() : AnalyticsConfig.Mapper {
    override fun map(rawConfig: AppConfigAndroid.ApplicationConfigurationAndroid): AnalyticsConfig {
        if (!rawConfig.hasPrivacyPreservingAnalyticsParameters() &&
            !rawConfig.privacyPreservingAnalyticsParameters.hasCommon() &&
            !rawConfig.privacyPreservingAnalyticsParameters.hasPpac()
        ) {
            throw ApplicationConfigurationInvalidException(
                message = "Analytics Parameters are missing"
            )
        }

        return rawConfig.mapAnalyticsConfig()
    }

    private fun AppConfigAndroid.ApplicationConfigurationAndroid.mapSafetyNet(): SafetyNetRequirementsContainer =
        this.privacyPreservingAnalyticsParameters.ppac.let {
            SafetyNetRequirementsContainer(
                requireBasicIntegrity = it
                    .requireBasicIntegrity,
                requireCTSProfileMatch = it
                    .requireCTSProfileMatch,
                requireEvaluationTypeBasic = it
                    .requireEvaluationTypeBasic,
                requireEvaluationTypeHardwareBacked = it
                    .requireEvaluationTypeHardwareBacked
            )
        }

    private fun AppConfigAndroid.ApplicationConfigurationAndroid.mapAnalyticsConfig(): AnalyticsConfigContainer =
        this.privacyPreservingAnalyticsParameters.common.let {
            AnalyticsConfigContainer(
                safetyNetRequirements = this.mapSafetyNet(),
                probabilityToSubmit = it
                    .probabilityToSubmit,
                hoursSinceTestResultToSubmitKeySubmissionMetadata = it
                    .hoursSinceTestResultToSubmitKeySubmissionMetadata,
                hoursSinceTestRegistrationToSubmitTestResultMetadata = it
                    .hoursSinceTestRegistrationToSubmitTestResultMetadata,
                probabilityToSubmitNewExposureWindows = it
                    .probabilityToSubmitExposureWindows
            )
        }

    data class AnalyticsConfigContainer(
        override val safetyNetRequirements: SafetyNetRequirements,
        override val probabilityToSubmit: Double,
        override val hoursSinceTestRegistrationToSubmitTestResultMetadata: Int,
        override val hoursSinceTestResultToSubmitKeySubmissionMetadata: Int,
        override val probabilityToSubmitNewExposureWindows: Double
    ) : AnalyticsConfig
}
