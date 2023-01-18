package de.rki.coronawarnapp.datadonation.analytics.modules.exposureriskmetadata

import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExposureRiskMetadataDonor @Inject constructor(
    private val riskLevelStorage: RiskLevelStorage,
    private val analyticsSettings: AnalyticsSettings
) : DonorModule {

    override suspend fun beginDonation(request: DonorModule.Request): DonorModule.Contribution {
        val previousMetadata = analyticsSettings.previousExposureRiskMetadata.first()

        val erMetadataBuilder = PpaData.ExposureRiskMetadata.newBuilder()

        buildEWMetadata(previousMetadata, erMetadataBuilder)
        buildPTMetadata(previousMetadata, erMetadataBuilder)

        val erMetadata = erMetadataBuilder.build()

        return ExposureRiskMetadataContribution(
            contributionProto = erMetadata,
            onContributionFinished = { wasSuccessful ->
                if (wasSuccessful) {
                    // overwrite data with current metadata
                    analyticsSettings.updatePreviousExposureRiskMetadata(erMetadata)
                }
            }
        )
    }

    private suspend fun buildEWMetadata(
        previousMetadata: PpaData.ExposureRiskMetadata?,
        builder: PpaData.ExposureRiskMetadata.Builder
    ) {
        val lastEWRiskResult = riskLevelStorage
            .lastSuccessfulEwRiskResult
            .first()

        val riskLevelEWForMetadata = lastEWRiskResult?.riskState.toMetadataRiskLevel()
        val mostRecentDateAtEWRiskLevel = lastEWRiskResult?.mostRecentDateAtRiskState?.epochSecond ?: -1

        builder
            .setRiskLevel(riskLevelEWForMetadata)
            .setRiskLevelChangedComparedToPreviousSubmission(
                previousMetadata != null && previousMetadata.riskLevel != riskLevelEWForMetadata
            ).setMostRecentDateAtRiskLevel(mostRecentDateAtEWRiskLevel)
            .dateChangedComparedToPreviousSubmission =
            previousMetadata != null && previousMetadata.mostRecentDateAtRiskLevel != mostRecentDateAtEWRiskLevel
    }

    private suspend fun buildPTMetadata(
        previousMetadata: PpaData.ExposureRiskMetadata?,
        builder: PpaData.ExposureRiskMetadata.Builder
    ) {
        val lastPTRiskResult = riskLevelStorage
            .lastSuccessfulPtRiskResult
            .first()

        val riskLevelPtForMetadata = lastPTRiskResult?.riskState.toMetadataRiskLevel()
        val mostRecentDateAtPtRiskLevel = lastPTRiskResult
            ?.mostRecentDateAtRiskState
            ?.epochSecond ?: -1

        builder
            .setPtRiskLevel(riskLevelPtForMetadata)
            .setPtRiskLevelChangedComparedToPreviousSubmission(
                previousMetadata != null && previousMetadata.ptRiskLevel != riskLevelPtForMetadata
            ).setPtMostRecentDateAtRiskLevel(mostRecentDateAtPtRiskLevel)
            .ptDateChangedComparedToPreviousSubmission =
            previousMetadata != null && previousMetadata.ptMostRecentDateAtRiskLevel != mostRecentDateAtPtRiskLevel
    }

    override suspend fun deleteData() {
        analyticsSettings.updatePreviousExposureRiskMetadata(null)
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

private fun RiskState?.toMetadataRiskLevel(): PpaData.PPARiskLevel =
    when (this) {
        RiskState.INCREASED_RISK -> PpaData.PPARiskLevel.RISK_LEVEL_HIGH
        else -> PpaData.PPARiskLevel.RISK_LEVEL_LOW
    }
