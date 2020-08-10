package de.rki.coronawarnapp.nearby

import android.app.Activity
import android.content.IntentSender
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationStatusCodes
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.exception.ENPermissionException
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import kotlinx.coroutines.launch

/**
 * Permission Helper for Exposure Notification permissions needed by the API
 *
 * In the current v1.3 of the Exposure Notification the user is asked for permission for the following
 * actions:
 *
 *  getTemporaryExposureKeyHistory
 *  start
 *
 *  The permission in the current state is implemented with a specific exception. If the Exposure Notification functions
 *  needs a specific permission to proceed an ApiException with a certain status will be raised.
 *  This needs to be handled accordingly in order to display the permission dialog to the user.
 *  @see handleException
 *
 * @property host
 * @property callback
 */

class InternalExposureNotificationPermissionHelper(
    private val host: Fragment,
    private val callback: Callback
) {

    interface Callback {
        fun onFailure(exception: Exception?)
        fun onStartPermissionGranted() {}
        fun onKeySharePermissionGranted(keys: List<TemporaryExposureKey>) {}
    }

    companion object {
        private val TAG: String? = InternalExposureNotificationPermissionHelper::class.simpleName
    }

    private var permissionResolutionInProgress = false

    /**
     * Function to request the permission to start tracing
     *
     * @see InternalExposureNotificationClient.asyncStart
     *
     */
    fun requestPermissionToStartTracing() {
        // Call start only if isEnabled() is false
        host.viewLifecycleOwner.lifecycleScope.launch {
            try {
                val isEnabled =
                    InternalExposureNotificationClient.asyncIsEnabled()
                if (!isEnabled) {
                    InternalExposureNotificationClient.asyncStart()
                }
                callback.onStartPermissionGranted()
            } catch (apiException: ApiException) {
                handleException(
                    apiException,
                    ResolutionRequestCodes.REQUEST_CODE_START_EXPOSURE_NOTIFICATION.code
                )
            } catch (exception: Exception) {
                returnError(exception)
            }
        }
    }

    /**
     * Function to request the permission get and ultimately share their own Temporary Exposure Keys
     *
     * @see InternalExposureNotificationClient.asyncGetExposureSummary
     *
     */
    fun requestPermissionToShareKeys() {

        host.viewLifecycleOwner.lifecycleScope.launch {
            try {
                val keys = InternalExposureNotificationClient.asyncGetTemporaryExposureKeyHistory()
                callback.onKeySharePermissionGranted(keys)
            } catch (apiException: ApiException) {
                handleException(
                    apiException,
                    ResolutionRequestCodes.REQUEST_CODE_GET_TEMP_EXPOSURE_KEY_HISTORY.code
                )
            } catch (exception: Exception) {
                returnError(exception)
            }
        }
    }

    /**
     * Will evaluate if the exception was raised because of a missing permission
     * If that is the case the permission dialog will be shown to the user
     *
     * @param apiException
     * @param resolutionRequestCode request code for the specific task the user has to give permission
     */
    private fun handleException(apiException: ApiException, resolutionRequestCode: Int) {
        if (permissionResolutionInProgress) {
            returnError(apiException)
            return
        }

        if (apiException.statusCode == ExposureNotificationStatusCodes.RESOLUTION_REQUIRED) {
            try {
                permissionResolutionInProgress = true
                apiException.status.startResolutionForResult(
                    host.activity,
                    resolutionRequestCode
                )
            } catch (e: IntentSender.SendIntentException) {
                returnError(e)
            }
        } else {
            returnError(apiException)
        }
    }

    /**
     * Returns the error to the callback
     *
     * @param exception
     */
    private fun returnError(exception: Exception) {
            exception.report(
                ExceptionCategory.EXPOSURENOTIFICATION,
                TAG,
                null
            )
        permissionResolutionInProgress = false
        callback.onFailure(exception)
    }

    /**
     * Function which can be invoked on the onActivityResult event to get the users decision if the permission was
     * given or not.
     *
     * Depending on the requested permission the corresponding function will be executed again to check if the permission
     * is now there and to execute the action immediately
     *
     * @param requestCode code of the requested permission
     * @param resultCode code of the user decision
     */
    fun onResolutionComplete(
        requestCode: Int,
        resultCode: Int
    ) {
        permissionResolutionInProgress = false

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ResolutionRequestCodes.REQUEST_CODE_START_EXPOSURE_NOTIFICATION.code
                -> requestPermissionToStartTracing()
                ResolutionRequestCodes.REQUEST_CODE_GET_TEMP_EXPOSURE_KEY_HISTORY.code
                -> requestPermissionToShareKeys()
            }
        } else {
            callback.onFailure(ENPermissionException())
        }
    }
}
