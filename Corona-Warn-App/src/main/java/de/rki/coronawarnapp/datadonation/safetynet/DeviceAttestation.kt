package de.rki.coronawarnapp.datadonation.safetynet

import de.rki.coronawarnapp.appconfig.SafetyNetRequirements

interface DeviceAttestation {

    suspend fun attest(request: Request): Result

    interface Request {
        val scenarioPayload: ByteArray
    }

    interface Result {
        val nonce: ByteArray
        val salt: ByteArray

        /**
         * If the attestation does not match the safetynet requirements, this will throw an exception
         */
        @Throws(SafetyNetException::class)
        fun requirePass(requirements: SafetyNetRequirements)
    }
}
