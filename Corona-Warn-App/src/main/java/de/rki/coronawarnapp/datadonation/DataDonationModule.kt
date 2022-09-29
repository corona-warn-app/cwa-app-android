package de.rki.coronawarnapp.datadonation

import dagger.Binds
import dagger.Module
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
interface DataDonationModule {
    @Binds
    fun deviceAttestation(safetyNet: CWASafetyNet): DeviceAttestation
}
