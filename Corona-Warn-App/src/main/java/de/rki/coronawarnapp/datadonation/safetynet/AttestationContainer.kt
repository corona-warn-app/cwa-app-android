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
        if (reqs.requireBasicIntegrity && !report.basicIntegrity) {
            throw SafetyNetException(
                Type.BASIC_INTEGRITY_REQUIRED,
                "Requirement 'basicIntegrity' not met (${report.advice})."
            )
        }

        if (reqs.requireCTSProfileMatch && !report.ctsProfileMatch) {
            throw SafetyNetException(
                Type.CTS_PROFILE_MATCH_REQUIRED,
                "Requirement 'ctsProfileMatch' not met (${report.advice})."
            )
        }

        if (reqs.requireBasicIntegrity && !report.evaluationTypes.contains("BASIC")) {
            throw SafetyNetException(
                Type.EVALUATION_TYPE_BASIC_REQUIRED,
                "Evaluation type 'BASIC' not met (${report.advice})."
            )
        }

        if (reqs.requireEvaluationTypeHardwareBacked && !report.evaluationTypes.contains("HARDWARE_BACKED")) {
            throw SafetyNetException(
                Type.EVALUATION_TYPE_HARDWARE_BACKED_REQUIRED,
                "Evaluation type 'HARDWARE_BACKED' not met (${report.advice})."
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
