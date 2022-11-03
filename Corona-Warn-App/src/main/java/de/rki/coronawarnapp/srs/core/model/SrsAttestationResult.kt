package de.rki.coronawarnapp.srs.core.model

import de.rki.coronawarnapp.appconfig.SafetyNetRequirements
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import de.rki.coronawarnapp.datadonation.safetynet.SafetyNetClientWrapper
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpacAndroid
import timber.log.Timber

internal data class SrsAttestationResult(
    val ourSalt: ByteArray,
    val report: SafetyNetClientWrapper.Report,
) : DeviceAttestation.Result {
    override val accessControlProtoBuf: PpacAndroid.PPACAndroid
        get() = PpacAndroid.PPACAndroid.newBuilder().build()

    override fun requirePass(requirements: SafetyNetRequirements) {
        Timber.v("requirePass(%s)", requirements)

        if (requirements.requireBasicIntegrity && !report.basicIntegrity) {
            Timber.w("Requirement 'basicIntegrity' not met (${report.advice}).")
        }

        if (requirements.requireCTSProfileMatch && !report.ctsProfileMatch) {
            Timber.w("Requirement 'ctsProfileMatch' not met (${report.advice}).")
        }

        if (requirements.requireBasicIntegrity && !report.evaluationTypes.contains("BASIC")) {
            Timber.w("Evaluation type 'BASIC' not met (${report.advice}).")
        }

        if (requirements.requireEvaluationTypeHardwareBacked && !report.evaluationTypes.contains("HARDWARE_BACKED")) {
            Timber.w("Evaluation type 'HARDWARE_BACKED' not met (${report.advice}).")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SrsAttestationResult

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
