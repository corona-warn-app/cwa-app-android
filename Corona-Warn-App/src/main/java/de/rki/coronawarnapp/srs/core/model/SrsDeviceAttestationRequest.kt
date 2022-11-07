package de.rki.coronawarnapp.srs.core.model

import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation

data class SrsDeviceAttestationRequest(
    override val scenarioPayload: ByteArray,
    override val configData: ConfigData? = null,
    override val checkDeviceTime: Boolean = true

) : DeviceAttestation.Request {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SrsDeviceAttestationRequest

        if (!scenarioPayload.contentEquals(other.scenarioPayload)) return false
        if (configData != other.configData) return false

        return true
    }

    override fun hashCode(): Int {
        var result = scenarioPayload.contentHashCode()
        result = 31 * result + (configData?.hashCode() ?: 0)
        return result
    }
}
