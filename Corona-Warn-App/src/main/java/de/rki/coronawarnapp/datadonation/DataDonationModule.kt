package de.rki.coronawarnapp.datadonation

import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.datadonation.safetynet.CWASafetyNet
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation

@Module
class DataDonationModule {
    @Provides
    fun deviceAttestation(safetyNet: CWASafetyNet): DeviceAttestation = safetyNet
}
