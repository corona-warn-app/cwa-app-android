package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission

import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeySubmissionStateDonor @Inject constructor() : DonorModule {

    override suspend fun startDonation(request: DonorModule.Request): DonorModule.Contribution {
        TODO("Not yet implemented")
    }
}
