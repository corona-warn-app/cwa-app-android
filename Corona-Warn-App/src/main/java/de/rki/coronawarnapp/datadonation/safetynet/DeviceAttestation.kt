package de.rki.coronawarnapp.datadonation.safetynet

import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.SafetyNetRequirements
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpacAndroid

interface DeviceAttestation {

    /**
     * Will return the attestation result against which you can check your requirements.
     * Will throw an exception if no attestation could be obtained (e.g. no network)
     */
    @Throws(SafetyNetException::class)
    suspend fun attest(request: Request): Result

    @Throws(SafetyNetException::class)
    suspend fun attest(
        request: Request,
        resultFactory: (ByteArray, SafetyNetClientWrapper.Report) -> Result
    ): Result

    interface Request {

        val configData: ConfigData?
            get() = null

        val checkDeviceTime: Boolean
            get() = true

        /**
         * e.g. for EventSurvey, a UUID, base64 encoded.
         */
        val scenarioPayload: ByteArray
    }

    interface Result {
        /**
         * The protobuf structure that you (Survey or Analytics) need for your own protobuf classes,
         * when sending data to the server.
         */
        val accessControlProtoBuf: PpacAndroid.PPACAndroid

        /**
         * If the attestation does not match the safetynet requirements, this will throw an exception
         * Pass your usecase specific instance.
         * Survey- and Analytics-Config will have implementation of this.
         */
        @Throws(SafetyNetException::class)
        fun requirePass(requirements: SafetyNetRequirements)
    }
}
