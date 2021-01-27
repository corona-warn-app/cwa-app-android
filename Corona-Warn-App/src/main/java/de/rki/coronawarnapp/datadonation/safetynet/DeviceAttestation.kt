package de.rki.coronawarnapp.datadonation.safetynet

interface DeviceAttestation {

    suspend fun attest(request: Request): Result

    interface Request {
        val scenarioPayload: ByteArray
    }

    interface Result {
        val nonce: ByteArray
        val salt: ByteArray
    }
}
