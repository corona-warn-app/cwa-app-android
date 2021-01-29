package de.rki.coronawarnapp.datadonation.safetynet

import de.rki.coronawarnapp.appconfig.SafetyNetRequirements
import de.rki.coronawarnapp.datadonation.safetynet.SafetyNetException.Type
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpacAndroid
import okio.ByteString.Companion.toByteString

internal data class AttestationContainer(
    private val ourSalt: ByteArray,
    private val report: SafetyNetClientWrapper.Report
) : DeviceAttestation.Result {
    override val accessControlProtoBuf: PpacAndroid.PPACAndroid
        get() = PpacAndroid.PPACAndroid.newBuilder().apply {
            salt = ourSalt.toByteString().base64()
            safetyNetJws = report.jwsResult
        }.build()

    override fun requirePass(reqs: SafetyNetRequirements) {
        val body = report.body

        if (reqs.requireBasicIntegrity && body.get("basicIntegrity")?.asBoolean != true) {
            throw SafetyNetException(Type.BASIC_INTEGRITY_REQUIRED, "Requirement 'basicIntegrity' not met.")
        }

        if (reqs.requireCTSProfileMatch && body.get("ctsProfileMatch")?.asBoolean != true) {
            throw SafetyNetException(Type.CTS_PROFILE_MATCH_REQUIRED, "Requirement 'ctsProfileMatch' not met.")
        }

        val evaluationType = body
            .get("evaluationType")
            ?.asString
            ?.split(",")
            ?.map { it.trim() }
            ?: emptyList()
        if (reqs.requireBasicIntegrity && !evaluationType.contains("BASIC")) {
            throw SafetyNetException(Type.EVALUATION_TYPE_BASIC_REQUIRED, "Requirement 'BASIC' not met.")
        }

        if (reqs.requireEvaluationTypeHardwareBacked && !evaluationType.contains("HARDWARE_BACKED")) {
            throw SafetyNetException(
                Type.EVALUATION_TYPE_HARDWARE_BACKED_REQUIRED,
                "Requirement 'HARDWARE_BACKED' not met."
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AttestationContainer

        if (!ourSalt.contentEquals(other.ourSalt)) return false
        if (report != other.report) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ourSalt.contentHashCode()
        result = 31 * result + report.hashCode()
        return result
    }
}
