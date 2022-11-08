package de.rki.coronawarnapp.srs.core.repository

import de.rki.coronawarnapp.datadonation.safetynet.SafetyNetException
import de.rki.coronawarnapp.server.protocols.internal.SubmissionPayloadOuterClass.SubmissionPayload.SubmissionType
import de.rki.coronawarnapp.srs.core.error.SrsSubmissionException.ErrorCode
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class TypeMappingTest {

    @Test
    fun toSubmissionType() {
        SrsSubmissionType.SRS_OTHER.toSubmissionType() shouldBe
            SubmissionType.SUBMISSION_TYPE_SRS_OTHER
        SrsSubmissionType.SRS_SELF_TEST.toSubmissionType() shouldBe
            SubmissionType.SUBMISSION_TYPE_SRS_SELF_TEST
        SrsSubmissionType.SRS_REGISTERED_RAT.toSubmissionType() shouldBe
            SubmissionType.SUBMISSION_TYPE_SRS_REGISTERED_RAT
        SrsSubmissionType.SRS_UNREGISTERED_RAT.toSubmissionType() shouldBe
            SubmissionType.SUBMISSION_TYPE_SRS_UNREGISTERED_RAT
        SrsSubmissionType.SRS_REGISTERED_PCR.toSubmissionType() shouldBe
            SubmissionType.SUBMISSION_TYPE_SRS_REGISTERED_PCR
        SrsSubmissionType.SRS_UNREGISTERED_PCR.toSubmissionType() shouldBe
            SubmissionType.SUBMISSION_TYPE_SRS_UNREGISTERED_PCR
        SrsSubmissionType.SRS_RAPID_PCR.toSubmissionType() shouldBe
            SubmissionType.SUBMISSION_TYPE_SRS_RAPID_PCR
    }

    @Test
    fun toSrsErrorType() {
        SafetyNetException.Type.APK_PACKAGE_NAME_MISMATCH.toSrsErrorType() shouldBe
            ErrorCode.APK_PACKAGE_NAME_MISMATCH
        SafetyNetException.Type.ATTESTATION_FAILED.toSrsErrorType() shouldBe
            ErrorCode.ATTESTATION_FAILED
        SafetyNetException.Type.ATTESTATION_REQUEST_FAILED.toSrsErrorType() shouldBe
            ErrorCode.ATTESTATION_REQUEST_FAILED
        SafetyNetException.Type.DEVICE_TIME_UNVERIFIED.toSrsErrorType() shouldBe
            ErrorCode.DEVICE_TIME_UNVERIFIED
        SafetyNetException.Type.NONCE_MISMATCH.toSrsErrorType() shouldBe
            ErrorCode.NONCE_MISMATCH
        SafetyNetException.Type.BASIC_INTEGRITY_REQUIRED.toSrsErrorType() shouldBe
            ErrorCode.BASIC_INTEGRITY_REQUIRED
        SafetyNetException.Type.CTS_PROFILE_MATCH_REQUIRED.toSrsErrorType() shouldBe
            ErrorCode.CTS_PROFILE_MATCH_REQUIRED
        SafetyNetException.Type.EVALUATION_TYPE_BASIC_REQUIRED.toSrsErrorType() shouldBe
            ErrorCode.EVALUATION_TYPE_BASIC_REQUIRED
        SafetyNetException.Type.EVALUATION_TYPE_HARDWARE_BACKED_REQUIRED.toSrsErrorType() shouldBe
            ErrorCode.EVALUATION_TYPE_HARDWARE_BACKED_REQUIRED
        SafetyNetException.Type.DEVICE_TIME_INCORRECT.toSrsErrorType() shouldBe
            ErrorCode.DEVICE_TIME_INCORRECT
        SafetyNetException.Type.PLAY_SERVICES_VERSION_MISMATCH.toSrsErrorType() shouldBe
            ErrorCode.PLAY_SERVICES_VERSION_MISMATCH
        SafetyNetException.Type.TIME_SINCE_ONBOARDING_UNVERIFIED.toSrsErrorType() shouldBe
            ErrorCode.TIME_SINCE_ONBOARDING_UNVERIFIED
        SafetyNetException.Type.INTERNAL_ERROR.toSrsErrorType() shouldBe
            ErrorCode.ATTESTATION_FAILED
    }
}
