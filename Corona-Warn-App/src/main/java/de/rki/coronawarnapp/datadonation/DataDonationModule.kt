package de.rki.coronawarnapp.datadonation

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.rki.coronawarnapp.datadonation.analytics.AnalyticsModule
import de.rki.coronawarnapp.datadonation.safetynet.CWASafetyNet
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import de.rki.coronawarnapp.datadonation.survey.SurveyModule

@InstallIn(SingletonComponent::class)
@Module(
    includes = [
        SurveyModule::class,
        AnalyticsModule::class
    ]
)
interface DataDonationModule {
    @Binds
    fun deviceAttestation(safetyNet: CWASafetyNet): DeviceAttestation
}
