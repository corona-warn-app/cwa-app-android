/******************************************************************************
 * Corona-Warn-App                                                            *
 *                                                                            *
 * SAP SE and all other contributors /                                        *
 * copyright owners license this file to you under the Apache                 *
 * License, Version 2.0 (the "License"); you may not use this                 *
 * file except in compliance with the License.                                *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * http://www.apache.org/licenses/LICENSE-2.0                                 *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing,                 *
 * software distributed under the License is distributed on an                *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY                     *
 * KIND, either express or implied.  See the License for the                  *
 * specific language governing permissions and limitations                    *
 * under the License.                                                         *
 ******************************************************************************/

package de.rki.coronawarnapp.http

import KeyExportFormat
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.exception.WebRequestException
import de.rki.coronawarnapp.exception.report
import de.rki.coronawarnapp.http.request.ApplicationConfigurationRequest
import de.rki.coronawarnapp.http.request.KeyFileRequest
import de.rki.coronawarnapp.http.request.KeySubmissionRequest
import de.rki.coronawarnapp.http.request.RegistrationTokenRequest
import de.rki.coronawarnapp.http.request.TanRequest
import de.rki.coronawarnapp.http.request.TestResultRequest
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass.ApplicationConfiguration
import de.rki.coronawarnapp.util.IndexHelper.convertToIndex
import org.json.JSONArray
import java.io.File
import java.util.ArrayList
import java.util.UUID
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object WebRequestBuilder {
    private val TAG: String? = WebRequestBuilder::class.simpleName

    private const val MAX_RETRIES = 3
    private const val BACKOFF_MULTIPLIER = 2F

    private val standardRetryPolicy by lazy {
        DefaultRetryPolicy(
            TimeVariables.getTransactionTimeout().toInt(),
            MAX_RETRIES,
            BACKOFF_MULTIPLIER
        )
    }

    suspend fun <T> asyncGetArrayListFromGenericRequest(
        url: String,
        parser: (JSONArray) -> ArrayList<T>
    ) =
        suspendCoroutine<ArrayList<T>> { cont ->
            val requestID = UUID.randomUUID()
            val getArrayListRequest = JsonArrayRequest(
                url, Response.Listener { response ->
                    Log.d(TAG, "$requestID: Response is $response")
                    cont.resume(parser(response))
                }, RequestErrorListener(requestID, cont)
            )
            RequestQueueHolder.addToRequestQueue(getArrayListRequest)
            Log.d(TAG, "$requestID: Added $url to queue.")
        }

    suspend fun asyncGetIndexBasedQueryURLsFromServer(url: String) =
        suspendCoroutine<Map<Long, String>> { cont ->
            val requestID = UUID.randomUUID()
            val getArrayListRequest = StringRequest(
                url, Response.Listener { response ->
                    if (BuildConfig.DEBUG) Log.d(TAG, "$requestID: Response is $response")
                    cont.resume(response.convertToIndex())
                }, RequestErrorListener(requestID, cont)
            )
            RequestQueueHolder.addToRequestQueue(getArrayListRequest)
            Log.d(TAG, "$requestID: Added $url to queue.")
        }

    /**
     * Retrieves Key Files from the Server based on a URL
     *
     * @param url the given URL
     */
    suspend fun asyncGetKeyFilesFromServer(url: String) =
        suspendCoroutine<File> { cont ->
            val requestID = UUID.randomUUID()
            val request = KeyFileRequest(
                url,
                requestID,
                null,
                Response.Listener { response ->
                    Log.v(requestID.toString(), "key file request successful. (${response.length()}B)")
                    cont.resume(response)
                },
                RequestErrorListener(requestID, cont)
            ).also { keyFileRequest ->
                keyFileRequest.retryPolicy = standardRetryPolicy
                keyFileRequest.setShouldCache(false)
                keyFileRequest.setShouldRetryServerErrors(true)
            }
            RequestQueueHolder.addToRequestQueue(request)
            Log.v(requestID.toString(), "Added $url to queue.")
        }

    suspend fun asyncGetApplicationConfigurationFromServer(url: String) =
        suspendCoroutine<ApplicationConfiguration> { cont ->
            val requestID = UUID.randomUUID()
            val getKeyBucketRequest =
                ApplicationConfigurationRequest(
                    url,
                    requestID,
                    null,
                    Response.Listener { response -> cont.resume(response) },
                    RequestErrorListener(requestID, cont)
                )
            RequestQueueHolder.addToRequestQueue(getKeyBucketRequest)
            Log.d(TAG, "$requestID: Added $url to queue.")
        }

    suspend fun asyncGetRegistrationToken(
        url: String,
        key: String,
        keyType: String
    ) =
        suspendCoroutine<String> { cont ->
            val requestID = UUID.randomUUID()
            val getRegistrationTokenRequest =
                RegistrationTokenRequest(
                    url,
                    requestID,
                    keyType,
                    key,
                    false,
                    standardRetryPolicy,
                    Response.Listener { response ->
                        Log.d(
                            TAG,
                            "$requestID: Registration Token Request successful"
                        )
                        cont.resume(response)
                    },
                    RequestErrorListener(requestID, cont)
                )
            RequestQueueHolder.addToRequestQueue(getRegistrationTokenRequest)
            Log.d(TAG, "$requestID: Added $url to queue.")
        }

    suspend fun asyncGetTestResult(
        url: String,
        registrationToken: String
    ) =
        suspendCoroutine<Int> { cont ->
            val requestID = UUID.randomUUID()
            val getTestResultRequest =
                TestResultRequest(
                    url,
                    requestID,
                    registrationToken,
                    false,
                    standardRetryPolicy,

                    Response.Listener { response ->
                        Log.d(
                            TAG,
                            "$requestID: Test Result Request successful"
                        )
                        cont.resume(response)
                    },
                    RequestErrorListener(requestID, cont)
                )
            RequestQueueHolder.addToRequestQueue(getTestResultRequest)
            Log.d(TAG, "$requestID: Added $url to queue.")
        }

    suspend fun asyncGetTan(
        url: String,
        registrationToken: String
    ) =
        suspendCoroutine<String> { cont ->
            val requestID = UUID.randomUUID()
            val getTANRequest =
                TanRequest(
                    url,
                    requestID,
                    registrationToken,
                    false,
                    standardRetryPolicy,

                    Response.Listener { response ->
                        Log.d(
                            TAG,
                            "$requestID: TAN Request successful"
                        )
                        cont.resume(response)
                    },
                    RequestErrorListener(requestID, cont)
                )
            RequestQueueHolder.addToRequestQueue(getTANRequest)
            Log.d(TAG, "$requestID: Added $url to queue.")
        }

    suspend fun asyncSubmitKeysToServer(
        url: String,
        authCode: String,
        faked: Boolean,
        keyList: List<KeyExportFormat.TemporaryExposureKey>
    ) =
        suspendCoroutine<Int> { cont ->
            val requestID = UUID.randomUUID()
            Log.d(TAG, "Writing ${keyList.size} Keys to the Submission Payload.")
            val submissionPayload = KeyExportFormat.SubmissionPayload.newBuilder()
                .addAllKeys(keyList)
                .build()
                .toByteArray()
            val submitKeysRequest =
                KeySubmissionRequest(
                    url,
                    requestID,
                    submissionPayload,
                    authCode,
                    faked,
                    standardRetryPolicy,
                    Response.Listener { response ->
                        Log.d(
                            TAG,
                            "$requestID: Key Submission Request successful."
                        )
                        cont.resume(response)
                    },
                    RequestErrorListener(requestID, cont)
                )
            RequestQueueHolder.addToRequestQueue(submitKeysRequest)
            Log.d(TAG, "$requestID: Added $url to queue.")
        }

    private class RequestErrorListener<T>(
        private val requestID: UUID,
        private val cont: Continuation<T>
    ) :
        Response.ErrorListener {
        override fun onErrorResponse(error: VolleyError?) {
            if (error != null) {
                val webRequestException = WebRequestException("an error occurred during a webrequest", error)
                webRequestException.report(de.rki.coronawarnapp.exception.ExceptionCategory.HTTP)
                cont.resumeWithException(webRequestException)
            } else {
                cont.resumeWithException(NullPointerException("the provided exception from volley was null"))
            }
        }
    }
}
