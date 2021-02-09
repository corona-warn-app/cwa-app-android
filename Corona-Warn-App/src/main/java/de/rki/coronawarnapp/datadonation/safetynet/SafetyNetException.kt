package de.rki.coronawarnapp.datadonation.safetynet

class SafetyNetException constructor(
    val type: Type,
    message: String,
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
