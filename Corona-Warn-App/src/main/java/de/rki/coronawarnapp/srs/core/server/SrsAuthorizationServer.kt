package de.rki.coronawarnapp.srs.core.server

import javax.inject.Inject
import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpacAndroid
import de.rki.coronawarnapp.server.protocols.internal.ppdd.SrsOtp
import de.rki.coronawarnapp.server.protocols.internal.ppdd.SrsOtpRequestAndroid
import de.rki.coronawarnapp.srs.core.model.SrsAuthorizationRequest
import de.rki.coronawarnapp.srs.core.model.SrsAuthorizationResponse
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.withContext
import timber.log.Timber

@Reusable
class SrsAuthorizationServer @Inject constructor(
    srsAuthorizationApi: Lazy<SrsAuthorizationApi>,
    private val dispatcherProvider: DispatcherProvider,
) {
    private val api = srsAuthorizationApi.get()

    suspend fun authorize(request: SrsAuthorizationRequest): SrsAuthorizationResponse =
        withContext(dispatcherProvider.IO) {
            Timber.tag(TAG).d("authorize(request=%s)", request)
            val srsOtpRequest = SrsOtpRequestAndroid.SRSOneTimePasswordRequestAndroid.newBuilder()
                .setPayload(
                    SrsOtp.SRSOneTimePassword.newBuilder().setOtp(request.srsOtp.uuid.toString()).build()
                )
                .setAuthentication(
                    PpacAndroid.PPACAndroid.newBuilder()
                        .setAndroidId(request.androidId)
                        .setSafetyNetJws(request.safetyNetJws)
                        .setSalt(request.salt)
                        .build()
                )
                .build()
            api.authenticate(srsOtpRequest)
        }

    companion object {
        val TAG = tag<SrsAuthorizationServer>()
    }
}
