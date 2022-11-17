package de.rki.coronawarnapp.srs.core.error

import android.content.Context
import androidx.annotation.StringRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.HasHumanReadableError
import de.rki.coronawarnapp.util.HumanReadableError
import timber.log.Timber

class SrsSubmissionException(
    val errorCode: ErrorCode,
    private val errorArgs: Array<Any> = emptyArray(),
    override val cause: Throwable? = null
) : Exception(errorCode.code, cause), HasHumanReadableError {

    enum class ErrorCode(val code: String, val textKey: TextKey) {
        DEVICE_TIME_INCORRECT("DEVICE_TIME_INCORRECT", TextKey.CHANGE_DEVICE_TIME),
        DEVICE_TIME_UNVERIFIED("DEVICE_TIME_UNVERIFIED", TextKey.CHANGE_DEVICE_TIME),
        SUBMISSION_TOO_EARLY("SUBMISSION_TOO_EARLY", TextKey.SUBMISSION_TOO_EARLY),
        TIME_SINCE_ONBOARDING_UNVERIFIED(
            "TIME_SINCE_ONBOARDING_UNVERIFIED",
            TextKey.TIME_SINCE_ONBOARDING_UNVERIFIED
        ),

        MIN_TIME_SINCE_ONBOARDING("MIN_TIME_SINCE_ONBOARDING", TextKey.TIME_SINCE_ONBOARDING_UNVERIFIED),

        SRS_OTP_CLIENT_ERROR("SRS_OTP_CLIENT_ERROR", TextKey.CALL_HOTLINE),
        SRS_OTP_NO_NETWORK("SRS_OTP_NO_NETWORK", TextKey.NO_NETWORK),
        SRS_OTP_SERVER_ERROR("SRS_OTP_SERVER_ERROR", TextKey.TRY_AGAIN_LATER),
        SRS_OTP_400("SRS_OTP_400", TextKey.CALL_HOTLINE),
        SRS_OTP_401("SRS_OTP_401", TextKey.CALL_HOTLINE),
        SRS_OTP_403("SRS_OTP_403", TextKey.CALL_HOTLINE),

        SRS_SUB_CLIENT_ERROR("SRS_SUB_CLIENT_ERROR", TextKey.CALL_HOTLINE),
        SRS_SUB_NO_NETWORK("SRS_SUB_NO_NETWORK", TextKey.NO_NETWORK),
        SRS_SUB_SERVER_ERROR("SRS_SUB_SERVER_ERROR", TextKey.TRY_AGAIN_LATER),
        SRS_SUB_400("SRS_SUB_400", TextKey.CALL_HOTLINE),
        SRS_SUB_403("SRS_SUB_403", TextKey.CALL_HOTLINE),

        // Local
        ANDROID_ID_INVALID_LOCAL("ANDROID_ID_INVALID_LOCAL", TextKey.DEVICE_NOT_TRUSTED),
        ATTESTATION_FAILED("ATTESTATION_FAILED", TextKey.TRY_AGAIN_LATER),
        ATTESTATION_REQUEST_FAILED("ATTESTATION_REQUEST_FAILED", TextKey.TRY_AGAIN_LATER),
        PLAY_SERVICES_VERSION_MISMATCH("PLAY_SERVICES_VERSION_MISMATCH", TextKey.UPDATE_PLAY_SERVICES),
        NONCE_MISMATCH("NONCE_MISMATCH", TextKey.TRY_AGAIN_LATER),
        BASIC_INTEGRITY_REQUIRED("BASIC_INTEGRITY_REQUIRED", TextKey.DEVICE_NOT_TRUSTED),
        CTS_PROFILE_MATCH_REQUIRED("CTS_PROFILE_MATCH_REQUIRED", TextKey.DEVICE_NOT_TRUSTED),
        EVALUATION_TYPE_BASIC_REQUIRED("EVALUATION_TYPE_BASIC_REQUIRED", TextKey.DEVICE_NOT_TRUSTED),
        EVALUATION_TYPE_HARDWARE_BACKED_REQUIRED(
            "EVALUATION_TYPE_HARDWARE_BACKED_REQUIRED",
            TextKey.DEVICE_NOT_TRUSTED
        ),

        // Server
        ANDROID_ID_INVALID("ANDROID_ID_INVALID", TextKey.DEVICE_NOT_TRUSTED),
        APK_CERTIFICATE_MISMATCH("APK_CERTIFICATE_MISMATCH", TextKey.DEVICE_NOT_TRUSTED),
        APK_PACKAGE_NAME_MISMATCH("APK_PACKAGE_NAME_MISMATCH", TextKey.DEVICE_NOT_TRUSTED),
        ATTESTATION_EXPIRED("ATTESTATION_EXPIRED", TextKey.TRY_AGAIN_LATER),
        DEVICE_QUOTA_EXCEEDED("DEVICE_QUOTA_EXCEEDED", TextKey.SUBMISSION_TOO_EARLY),
        JWS_SIGNATURE_VERIFICATION_FAILED("JWS_SIGNATURE_VERIFICATION_FAILED", TextKey.TRY_AGAIN_LATER),
        SALT_REDEEMED("SALT_REDEEMED", TextKey.TRY_AGAIN_LATER);

        enum class TextKey(
            @StringRes val title: Int = R.string.srs_error_title,
            @StringRes val message: Int,
        ) {
            CALL_HOTLINE(message = R.string.srs_error_call_hotline),
            CHANGE_DEVICE_TIME(message = R.string.srs_error_device_time),
            DEVICE_NOT_TRUSTED(message = R.string.srs_error_device_not_trusted),
            NO_NETWORK(message = R.string.srs_error_no_network),
            SUBMISSION_TOO_EARLY(message = R.string.srs_error_submission_too_early),
            TIME_SINCE_ONBOARDING_UNVERIFIED(message = R.string.srs_error_time_since_onboarding_unverified),
            TRY_AGAIN_LATER(message = R.string.srs_error_try_again_later),
            UPDATE_PLAY_SERVICES(message = R.string.srs_error_update_play_services),
        }

        companion object {
            fun fromAuthErrorCode(code: String) = values().find { code == it.code } ?: run {
                Timber.e("ErrorCode=%s was not found -> return SRS_OTP_SERVER_ERROR as fallback", code)
                SRS_OTP_SERVER_ERROR
            }
        }
    }

    override fun toHumanReadableError(context: Context) = HumanReadableError(
        title = context.getString(errorCode.textKey.title),
        description = context.getString(errorCode.textKey.message, *errorArgs) + " (${errorCode.code})"
    )
}
