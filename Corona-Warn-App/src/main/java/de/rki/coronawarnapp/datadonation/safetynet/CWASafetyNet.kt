package de.rki.coronawarnapp.datadonation.safetynet

import de.rki.coronawarnapp.appconfig.SafetyNetRequirements
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpacAndroid
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CWASafetyNet @Inject constructor() : DeviceAttestation {
    override suspend fun attest(request: DeviceAttestation.Request): DeviceAttestation.Result {
        return object : DeviceAttestation.Result {
            override val accessControlProtoBuf: PpacAndroid.PPACAndroid = PpacAndroid.PPACAndroid.getDefaultInstance()

            override fun requirePass(requirements: SafetyNetRequirements) {
                // Passed
            }
        }
    }
}
