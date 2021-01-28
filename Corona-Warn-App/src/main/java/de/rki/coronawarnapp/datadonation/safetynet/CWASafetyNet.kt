package de.rki.coronawarnapp.datadonation.safetynet

import de.rki.coronawarnapp.appconfig.SafetyNetRequirements
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CWASafetyNet @Inject constructor() : DeviceAttestation {
    override suspend fun attest(request: DeviceAttestation.Request): DeviceAttestation.Result {
        return object : DeviceAttestation.Result {
            override val nonce: ByteArray
                get() = ByteArray(1)
            override val salt: ByteArray
                get() = ByteArray(1)

            override fun requirePass(requirements: SafetyNetRequirements) {
                throw SafetyNetException("TODO")
            }
        }
    }
}
