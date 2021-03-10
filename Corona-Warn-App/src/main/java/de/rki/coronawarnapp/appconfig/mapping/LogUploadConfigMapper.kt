package de.rki.coronawarnapp.appconfig.mapping

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.LogUploadConfig
import de.rki.coronawarnapp.appconfig.SafetyNetRequirements
import de.rki.coronawarnapp.appconfig.SafetyNetRequirementsContainer
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid
import timber.log.Timber
import javax.inject.Inject

@Reusable
class LogUploadConfigMapper @Inject constructor() : LogUploadConfig.Mapper {

    override fun map(rawConfig: AppConfigAndroid.ApplicationConfigurationAndroid): LogUploadConfig {
        if (!rawConfig.hasErrorLogSharingParameters()) {
            Timber.w("No error log sharing parameters found, returning defaults.")
            return LogUploadConfigContainer()
        }
        return LogUploadConfigContainer(
            safetyNetRequirements = rawConfig.mapSafetyNet()
        )
    }

    private fun AppConfigAndroid.ApplicationConfigurationAndroid.mapSafetyNet(): SafetyNetRequirementsContainer {
        return privacyPreservingAnalyticsParameters.ppac.let {
            SafetyNetRequirementsContainer(
                requireBasicIntegrity = it.requireBasicIntegrity,
                requireCTSProfileMatch = it.requireCTSProfileMatch,
                requireEvaluationTypeBasic = it.requireEvaluationTypeBasic,
                requireEvaluationTypeHardwareBacked = it.requireEvaluationTypeHardwareBacked
            )
        }
    }

    data class LogUploadConfigContainer(
        override val safetyNetRequirements: SafetyNetRequirements = SafetyNetRequirementsContainer()
    ) : LogUploadConfig
}
