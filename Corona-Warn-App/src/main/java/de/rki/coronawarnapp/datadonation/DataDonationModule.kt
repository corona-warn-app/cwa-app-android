package de.rki.coronawarnapp.datadonation

import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.datadonation.analytics.AnalyticsModule
import de.rki.coronawarnapp.datadonation.safetynet.CWASafetyNet
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import de.rki.coronawarnapp.datadonation.survey.SurveyModule

@Module(
    includes = [
        SurveyModule::class,
        AnalyticsModule::class
    ]
)
class DataDonationModule {
    @Provides
    fun deviceAttestation(safetyNet: CWASafetyNet): DeviceAttestation = safetyNet
}
