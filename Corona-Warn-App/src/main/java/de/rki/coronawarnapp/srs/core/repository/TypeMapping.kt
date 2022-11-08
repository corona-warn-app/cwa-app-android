package de.rki.coronawarnapp.srs.core.repository

import de.rki.coronawarnapp.datadonation.safetynet.SafetyNetException.Type
import de.rki.coronawarnapp.server.protocols.internal.SubmissionPayloadOuterClass.SubmissionPayload.SubmissionType
import de.rki.coronawarnapp.srs.core.error.SrsSubmissionException.ErrorCode
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType

internal fun SrsSubmissionType.toSubmissionType() = when (this) {
    SrsSubmissionType.SRS_SELF_TEST -> SubmissionType.SUBMISSION_TYPE_SRS_SELF_TEST
    SrsSubmissionType.SRS_REGISTERED_RAT -> SubmissionType.SUBMISSION_TYPE_SRS_REGISTERED_RAT
    SrsSubmissionType.SRS_UNREGISTERED_RAT -> SubmissionType.SUBMISSION_TYPE_SRS_UNREGISTERED_RAT
    SrsSubmissionType.SRS_REGISTERED_PCR -> SubmissionType.SUBMISSION_TYPE_SRS_REGISTERED_PCR
    SrsSubmissionType.SRS_UNREGISTERED_PCR -> SubmissionType.SUBMISSION_TYPE_SRS_UNREGISTERED_PCR
    SrsSubmissionType.SRS_RAPID_PCR -> SubmissionType.SUBMISSION_TYPE_SRS_RAPID_PCR
    SrsSubmissionType.SRS_OTHER -> SubmissionType.SUBMISSION_TYPE_SRS_OTHER
}

internal fun Type.toSrsErrorType() = when (this) {
    Type.APK_PACKAGE_NAME_MISMATCH -> ErrorCode.APK_PACKAGE_NAME_MISMATCH
    Type.ATTESTATION_FAILED -> ErrorCode.ATTESTATION_FAILED
    Type.ATTESTATION_REQUEST_FAILED -> ErrorCode.ATTESTATION_REQUEST_FAILED
    Type.DEVICE_TIME_UNVERIFIED -> ErrorCode.DEVICE_TIME_UNVERIFIED
    Type.NONCE_MISMATCH -> ErrorCode.NONCE_MISMATCH
    Type.BASIC_INTEGRITY_REQUIRED -> ErrorCode.BASIC_INTEGRITY_REQUIRED
    Type.CTS_PROFILE_MATCH_REQUIRED -> ErrorCode.CTS_PROFILE_MATCH_REQUIRED
    Type.EVALUATION_TYPE_BASIC_REQUIRED -> ErrorCode.EVALUATION_TYPE_BASIC_REQUIRED
    Type.EVALUATION_TYPE_HARDWARE_BACKED_REQUIRED -> ErrorCode.EVALUATION_TYPE_HARDWARE_BACKED_REQUIRED
    Type.DEVICE_TIME_INCORRECT -> ErrorCode.DEVICE_TIME_INCORRECT
    Type.PLAY_SERVICES_VERSION_MISMATCH -> ErrorCode.PLAY_SERVICES_VERSION_MISMATCH
    Type.TIME_SINCE_ONBOARDING_UNVERIFIED -> ErrorCode.TIME_SINCE_ONBOARDING_UNVERIFIED
    Type.INTERNAL_ERROR -> ErrorCode.ATTESTATION_FAILED
}
