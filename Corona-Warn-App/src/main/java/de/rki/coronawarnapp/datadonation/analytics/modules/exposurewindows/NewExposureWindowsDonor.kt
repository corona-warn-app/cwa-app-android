package de.rki.coronawarnapp.datadonation.analytics.modules.exposurewindows

import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewExposureWindowsDonor @Inject constructor() : DonorModule {

    override suspend fun beginDonation(request: DonorModule.Request): DonorModule.Contribution {
        return CollectedData(
            protobuf = Any(),
            onContributionFinished = { success ->
                // TODO
            }
        )
    }

    override suspend fun deleteData() {
        // TODO
    }

    data class CollectedData(
        val protobuf: Any,
        val onContributionFinished: suspend (Boolean) -> Unit
    ) : DonorModule.Contribution {
        override suspend fun injectData(protobufContainer: PpaData.PPADataAndroid.Builder) {
            // TODO "Add this specific protobuf to the top level protobuf container"
        }

        override suspend fun finishDonation(successful: Boolean) {
            onContributionFinished(successful)
        }
    }
}
