package de.rki.coronawarnapp.appconfig.mapping

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.SafetyNetRequirementsContainer
import de.rki.coronawarnapp.appconfig.SelfReportSubmissionCommonContainer

import de.rki.coronawarnapp.appconfig.SelfReportSubmissionConfig
import de.rki.coronawarnapp.appconfig.SelfReportSubmissionConfigContainer
import de.rki.coronawarnapp.appconfig.SrsPlausibleDeniabilityParameters
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid
import de.rki.coronawarnapp.server.protocols.internal.v2.PpddSrsParameters
import timber.log.Timber
import java.time.Duration
import javax.inject.Inject

@Reusable
class SelfReportSubmissionConfigMapper @Inject constructor() : SelfReportSubmissionConfig.Mapper {
    override fun map(rawConfig: AppConfigAndroid.ApplicationConfigurationAndroid): SelfReportSubmissionConfig = try {
        when {
            !rawConfig.hasSelfReportParameters() -> {
                Timber.d("No SelfReportParameters -> set to default")
                SelfReportSubmissionConfigContainer.DEFAULT
            }

            else -> rawConfig.selfReportParameters.map()
        }
    } catch (e: Exception) {
        Timber.d(e, "SelfReportSubmissionConfigMapper failed -> returning default")
        SelfReportSubmissionConfigContainer.DEFAULT
    }

    fun PpddSrsParameters.PPDDSelfReportSubmissionParametersAndroid.map(): SelfReportSubmissionConfigContainer {
        val common = if (hasCommon()) {
            SelfReportSubmissionCommonContainer(
                timeSinceOnboardingInHours = if (common.timeSinceOnboardingInHours <= 0) {
                    Timber.d("Faulty timeSinceOnboardingInHours -> set to default")
                    SelfReportSubmissionCommonContainer.DEFAULT_HOURS
                } else {
                    Duration.ofHours(common.timeSinceOnboardingInHours.toLong())
                },
                timeBetweenSubmissionsInDays = if (common.timeBetweenSubmissionsInDays <= 0) {
                    Timber.d("Faulty timeBetweenSubmissionsInDays -> set to default")
                    SelfReportSubmissionCommonContainer.DEFAULT_DAYS
                } else {
                    Duration.ofDays(common.timeBetweenSubmissionsInDays.toLong())
                },

                plausibleDeniabilityParameters = if (common.hasPlausibleDeniabilityParameters()) {
                    SrsPlausibleDeniabilityParameters(
                        minRequestPaddingBytes = common.plausibleDeniabilityParameters.minRequestPaddingBytes,
                        maxRequestPaddingBytes = common.plausibleDeniabilityParameters.maxRequestPaddingBytes
                    )
                } else {
                    Timber.d("No plausibleDeniabilityParameters -> set to default")
                    SrsPlausibleDeniabilityParameters()
                }
            )
        } else {
            Timber.d("No Common -> set to default")
            SelfReportSubmissionCommonContainer()
        }

        val ppac = if (hasPpac()) {
            SafetyNetRequirementsContainer(
                requireBasicIntegrity = ppac.requireBasicIntegrity,
                requireCTSProfileMatch = ppac.requireCTSProfileMatch,
                requireEvaluationTypeBasic = ppac.requireEvaluationTypeBasic,
                requireEvaluationTypeHardwareBacked = ppac.requireEvaluationTypeHardwareBacked
            )
        } else {
            Timber.d("No Ppac -> set to default")
            SafetyNetRequirementsContainer()
        }

        return SelfReportSubmissionConfigContainer(
            common = common,
            ppac = ppac
        )
    }
}
