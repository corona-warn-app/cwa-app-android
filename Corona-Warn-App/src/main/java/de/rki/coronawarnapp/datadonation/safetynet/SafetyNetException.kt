package de.rki.coronawarnapp.datadonation.safetynet

import de.rki.coronawarnapp.R

class SafetyNetException constructor(
    val type: Type,
    message: String? = null,
    cause: Throwable? = null
) : Exception("$type: $message", cause) {

    enum class Type {
        // TRY_AGAIN_LATER (Text Key)
        APK_PACKAGE_NAME_MISMATCH,
        ATTESTATION_FAILED,
        ATTESTATION_REQUEST_FAILED,
        DEVICE_TIME_UNVERIFIED,
        NONCE_MISMATCH,

        // DEVICE_NOT_TRUSTED (Text Key)
        BASIC_INTEGRITY_REQUIRED,
        CTS_PROFILE_MATCH_REQUIRED,
        EVALUATION_TYPE_BASIC_REQUIRED,
        EVALUATION_TYPE_HARDWARE_BACKED_REQUIRED,

        // CHANGE_DEVICE_TIME (Text Key)
        DEVICE_TIME_INCORRECT,

        // UPDATE_PLAY_SERVICES (Text Key)
        PLAY_SERVICES_VERSION_MISMATCH,

        // TIME_SINCE_ONBOARDING_UNVERIFIED (Text Key)
        TIME_SINCE_ONBOARDING_UNVERIFIED
    }
}

fun SafetyNetException.errorMsgRes(): Int = when (type) {
    SafetyNetException.Type.APK_PACKAGE_NAME_MISMATCH,
    SafetyNetException.Type.ATTESTATION_FAILED,
    SafetyNetException.Type.ATTESTATION_REQUEST_FAILED,
    SafetyNetException.Type.DEVICE_TIME_UNVERIFIED,
    SafetyNetException.Type.NONCE_MISMATCH ->
        R.string.datadonation_details_survey_consent_error_TRY_AGAIN_LATER
    SafetyNetException.Type.BASIC_INTEGRITY_REQUIRED,
    SafetyNetException.Type.CTS_PROFILE_MATCH_REQUIRED,
    SafetyNetException.Type.EVALUATION_TYPE_BASIC_REQUIRED,
    SafetyNetException.Type.EVALUATION_TYPE_HARDWARE_BACKED_REQUIRED ->
        R.string.datadonation_details_survey_consent_error_DEVICE_NOT_TRUSTED
    SafetyNetException.Type.DEVICE_TIME_INCORRECT ->
        R.string.datadonation_details_survey_consent_error_CHANGE_DEVICE_TIME
    SafetyNetException.Type.PLAY_SERVICES_VERSION_MISMATCH ->
        R.string.datadonation_details_survey_consent_error_UPDATE_PLAY_SERVICES
    SafetyNetException.Type.TIME_SINCE_ONBOARDING_UNVERIFIED ->
        R.string.datadonation_details_survey_consent_error_TIME_SINCE_ONBOARDING_UNVERIFIED
}
