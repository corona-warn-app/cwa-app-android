package de.rki.coronawarnapp.datadonation.analytics.modules.exposureriskmetadata

import de.rki.coronawarnapp.datadonation.analytics.common.toMetadataRiskLevel
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.analytics.storage.AnalyticsSettings
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.risk.tryLatestEwResultsWithDefaults
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExposureRiskMetadataDonor @Inject constructor(
    private val riskLevelStorage: RiskLevelStorage,
    private val analyticsSettings: AnalyticsSettings
) : DonorModule {

    override suspend fun beginDonation(request: DonorModule.Request): DonorModule.Contribution {
        val previousMetadata = analyticsSettings.previousExposureRiskMetadata.value

        val lastRiskResult = riskLevelStorage
            .latestAndLastSuccessfulEwRiskLevelResult
            .first()
            .tryLatestEwResultsWithDefaults()
            .lastCalculated

        val riskLevelForMetadata = lastRiskResult.toMetadataRiskLevel()
        val mostRecentDateAtRiskLevel = lastRiskResult.mostRecentDateAtRiskState?.seconds ?: -1

        val newMetadata = PpaData.ExposureRiskMetadata.newBuilder()
            .setRiskLevel(riskLevelForMetadata)
            .setRiskLevelChangedComparedToPreviousSubmission(previousMetadata?.riskLevel != riskLevelForMetadata)
            .setMostRecentDateAtRiskLevel(mostRecentDateAtRiskLevel)
            .setDateChangedComparedToPreviousSubmission(
                previousMetadata?.mostRecentDateAtRiskLevel != mostRecentDateAtRiskLevel
            )
            .build()

        return ExposureRiskMetadataContribution(
            contributionProto = newMetadata,
            onContributionFinished = {
                if (it) {
                    analyticsSettings.previousExposureRiskMetadata.update {
                        newMetadata
                    }
                } else {
                    analyticsSettings.previousExposureRiskMetadata.update {
                        null
                    }
                }
            }
        )
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
