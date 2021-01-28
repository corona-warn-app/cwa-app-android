package de.rki.coronawarnapp.datadonation.safetynet

import de.rki.coronawarnapp.appconfig.SafetyNetRequirements
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpacAndroid

interface DeviceAttestation {

    /**
     * Will return the attestation result against which you can check your requirements.
     * Will throw an exception if no attestation could be obtained (e.g. no network)
     */
    @Throws(SafetyNetException::class)
    suspend fun attest(request: Request): Result

    interface Request {
        /**
         * e.g. for EventSurvey, a UUID, base64 encoded.
         */
        val scenarioPayload: ByteArray
    }

    interface Result {
        val accessControlProtoBuf: PpacAndroid.PPACAndroid

        /**
         * If the attestation does not match the safetynet requirements, this will throw an exception
         */
        @Throws(SafetyNetException::class)
        fun requirePass(requirements: SafetyNetRequirements)
    }
}
