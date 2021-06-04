package de.rki.coronawarnapp.covidcertificate.server

import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.coronatest.type.RegistrationToken
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.encryption.rsa.RSAKey
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@Reusable
class CovidCertificateServer @Inject constructor(
    private val dccApi: Lazy<CovidCertificateApiV1>,
    private val dispatcherProvider: DispatcherProvider
) {

    private val api: CovidCertificateApiV1
        get() = dccApi.get()

    suspend fun registerPublicKeyForTest(
        testRegistrationToken: RegistrationToken,
        publicKey: RSAKey.Public,
    ): Unit = withContext(dispatcherProvider.IO) {
        Timber.tag(TAG).v("registerPublicKeyForTest(token=%s, key=%s)", testRegistrationToken, publicKey)
        api.sendPublicKey(
            requestBody = CovidCertificateApiV1.PublicKeyUploadRequest(
                registrationToken = testRegistrationToken,
                publicKey = publicKey.base64
            )
        )
    }

    @Throws(DccPendingException::class)
    suspend fun requestCertificateForTest(
        testRegistrationToken: RegistrationToken,
    ): TestCertificateComponents = withContext(dispatcherProvider.IO) {
        Timber.tag(TAG).v("requestCertificateForTest(token=%s)", testRegistrationToken)
        val response = api.getComponents(
            requestBody = CovidCertificateApiV1.ComponentsRequest(testRegistrationToken)
        )
        // TODO replace with InvalidTestCertificateException + correct error codes
        if (response.code() == 202) throw DccPendingException()
        val result = response.body() ?: throw Exception()
        TestCertificateComponents(
            dataEncryptionKeyBase64 = result.dek,
            encryptedCoseTestCertificateBase64 = result.dcc
        )
    }

    companion object {
        private const val TAG = "CovidCertificateServer"
    }
}
