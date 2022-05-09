package de.rki.coronawarnapp.datadonation.analytics.modules.exposureriskmetadata

import de.rki.coronawarnapp.datadonation.analytics.common.toMetadataRiskLevel
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.presencetracing.risk.PtRiskLevelResult
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import kotlinx.coroutines.flow.first
import org.joda.time.DateTimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExposureRiskMetadataDonor @Inject constructor(
    private val riskLevelStorage: RiskLevelStorage,
    private val analyticsSettings: AnalyticsSettings
) : DonorModule {

    override suspend fun beginDonation(request: DonorModule.Request): DonorModule.Contribution {
        val previousMetadata = analyticsSettings.previousExposureRiskMetadata.value

        val erMetadataBuilder = PpaData.ExposureRiskMetadata.newBuilder()

        buildEWMetadata(previousMetadata, erMetadataBuilder)
        buildPTMetadata(previousMetadata, erMetadataBuilder)

        val erMetadata = erMetadataBuilder.build()

        return ExposureRiskMetadataContribution(
            contributionProto = erMetadata,
            onContributionFinished = { wasSuccessful ->
                if (wasSuccessful) {
                    // overwrite data with current metadata
                    analyticsSettings.previousExposureRiskMetadata.update {
                        erMetadata
                    }
                }
            }
        )
    }

    private suspend fun buildEWMetadata(
        previousMetadata: PpaData.ExposureRiskMetadata?,
        builder: PpaData.ExposureRiskMetadata.Builder
    ) {
        val lastEWRiskResult = riskLevelStorage
            .latestAndLastSuccessfulCombinedEwPtRiskLevelResult
            .first()
            .lastCalculated
            .ewRiskLevelResult

        val riskLevelEWForMetadata = lastEWRiskResult.riskState.toMetadataRiskLevel()
        val mostRecentDateAtEWRiskLevel = lastEWRiskResult.mostRecentDateAtRiskState?.seconds ?: -1

        builder
            .setRiskLevel(riskLevelEWForMetadata)
            .setRiskLevelChangedComparedToPreviousSubmission(previousMetadata?.riskLevel != riskLevelEWForMetadata)
            .setMostRecentDateAtRiskLevel(mostRecentDateAtEWRiskLevel).dateChangedComparedToPreviousSubmission =
            previousMetadata?.mostRecentDateAtRiskLevel != mostRecentDateAtEWRiskLevel
    }

    private suspend fun buildPTMetadata(
        previousMetadata: PpaData.ExposureRiskMetadata?,
        builder: PpaData.ExposureRiskMetadata.Builder
    ) {
        val lastPTRiskResult = riskLevelStorage
            .latestAndLastSuccessfulCombinedEwPtRiskLevelResult
            .first()
            .lastCalculated
            .ptRiskLevelResult

        val riskLevelPtForMetadata = lastPTRiskResult.toMetadataRiskLevel()
        val mostRecentDateAtPtRiskLevel = lastPTRiskResult
            .mostRecentDateAtRiskState
            ?.toDateTimeAtStartOfDay(DateTimeZone.UTC)
            ?.toInstant()
            ?.seconds ?: -1

        builder
            .setPtRiskLevel(riskLevelPtForMetadata)
            .setPtRiskLevelChangedComparedToPreviousSubmission(previousMetadata?.ptRiskLevel != riskLevelPtForMetadata)
            .setPtMostRecentDateAtRiskLevel(mostRecentDateAtPtRiskLevel).ptDateChangedComparedToPreviousSubmission =
            previousMetadata?.ptMostRecentDateAtRiskLevel != mostRecentDateAtPtRiskLevel
    }

    override suspend fun deleteData() {
        analyticsSettings.previousExposureRiskMetadata.update {
            null
        }
    }

    data class ExposureRiskMetadataContribution(
        val contributionProto: PpaData.ExposureRiskMetadata,
        val onContributionFinished: suspend (Boolean) -> Unit
    ) : DonorModule.Contribution {
        override suspend fun injectData(protobufContainer: PpaData.PPADataAndroid.Builder) {
            protobufContainer.addExposureRiskMetadataSet(contributionProto)
        }

        override suspend fun finishDonation(successful: Boolean) {
            onContributionFinished(successful)
        }
    }
}

private fun PtRiskLevelResult.toMetadataRiskLevel(): PpaData.PPARiskLevel =
    when (riskState) {
        RiskState.LOW_RISK -> PpaData.PPARiskLevel.RISK_LEVEL_LOW
        RiskState.INCREASED_RISK -> PpaData.PPARiskLevel.RISK_LEVEL_HIGH
        else -> PpaData.PPARiskLevel.RISK_LEVEL_UNKNOWN
    }
