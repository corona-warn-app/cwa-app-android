package de.rki.coronawarnapp.datadonation

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.rki.coronawarnapp.datadonation.safetynet.CWASafetyNet
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation

@InstallIn(SingletonComponent::class)
@Module
interface DataDonationModule {
    @Binds
    fun deviceAttestation(safetyNet: CWASafetyNet): DeviceAttestation
}
