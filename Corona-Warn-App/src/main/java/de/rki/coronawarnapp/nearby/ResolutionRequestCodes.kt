package de.rki.coronawarnapp.nearby

/**
 * Enum class to reflect the request codes of the different
 * permissions for the Exposure Notification API
 *
 * @property code
 */
enum class ResolutionRequestCodes(val code: Int) {
    REQUEST_CODE_START_EXPOSURE_NOTIFICATION(
        ResolutionRequestCodeConstants.REQUEST_CODE_START_EXPOSURE_NOTIFICATION_CODE
    ),
    REQUEST_CODE_GET_TEMP_EXPOSURE_KEY_HISTORY(
        ResolutionRequestCodeConstants.REQUEST_CODE_GET_TEMP_EXPOSURE_KEY_HISTORY_CODE
    )
}
