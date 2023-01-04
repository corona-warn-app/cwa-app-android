package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.srs

import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import timber.log.Timber
import javax.inject.Singleton

@Singleton
class AnalyticsSrsKeySubmissionDonor(
    private val repository: AnalyticsKeySubmissionRepository
) : DonorModule {
    override suspend fun beginDonation(request: DonorModule.Request): DonorModule.Contribution {
        val srsPpaData = repository.srsPpaData()
        return if (srsPpaData != null) {
            Timber.d("srsPpaData=%s", srsPpaData)
            object : DonorModule.Contribution {
                override suspend fun injectData(protobufContainer: PpaData.PPADataAndroid.Builder) {
                    protobufContainer.addKeySubmissionMetadataSet(srsPpaData)
                }

                override suspend fun finishDonation(successful: Boolean) {
                    if (successful) {
                        Timber.d("finishDonation -> reset Srs Ppa data")
                        repository.reset()
                    }
                }
            }
        } else {
            Timber.d("No donation for SrsPpaData ")
            NoContribution
        }
    }

    override suspend fun deleteData() {
        Timber.d("deleteData")
        repository.reset()
    }

    object NoContribution : DonorModule.Contribution {
        override suspend fun injectData(protobufContainer: PpaData.PPADataAndroid.Builder) = Unit
        override suspend fun finishDonation(successful: Boolean) = Unit
    }
}
