package de.rki.coronawarnapp.datadonation.safetynet

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CWASafetyNet @Inject constructor() : DeviceAttestation {
    override suspend fun attest(request: DeviceAttestation.Request): DeviceAttestation.Result {
        TODO("Not yet implemented")
    }
}
