package de.rki.coronawarnapp.covidcertificate.test.core.server

import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.coronatest.type.RegistrationToken
import de.rki.coronawarnapp.covidcertificate.common.exception.TestCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.TestCertificateException.ErrorCode.DCC_COMP_202
import de.rki.coronawarnapp.covidcertificate.common.exception.TestCertificateException.ErrorCode.DCC_COMP_400
import de.rki.coronawarnapp.covidcertificate.common.exception.TestCertificateException.ErrorCode.DCC_COMP_404
import de.rki.coronawarnapp.covidcertificate.common.exception.TestCertificateException.ErrorCode.DCC_COMP_410
import de.rki.coronawarnapp.covidcertificate.common.exception.TestCertificateException.ErrorCode.DCC_COMP_412
import de.rki.coronawarnapp.covidcertificate.common.exception.TestCertificateException.ErrorCode.DCC_COMP_500
import de.rki.coronawarnapp.covidcertificate.common.exception.TestCertificateException.ErrorCode.DCC_COMP_500_LAB_INVALID_RESPONSE
import de.rki.coronawarnapp.covidcertificate.common.exception.TestCertificateException.ErrorCode.DCC_COMP_500_SIGNING_CLIENT_ERROR
import de.rki.coronawarnapp.covidcertificate.common.exception.TestCertificateException.ErrorCode.DCC_COMP_500_SIGNING_SERVER_ERROR
import de.rki.coronawarnapp.covidcertificate.common.exception.TestCertificateException.ErrorCode.DCC_COMP_NO_NETWORK
import de.rki.coronawarnapp.covidcertificate.common.exception.TestCertificateException.ErrorCode.PKR_400
import de.rki.coronawarnapp.covidcertificate.common.exception.TestCertificateException.ErrorCode.PKR_403
import de.rki.coronawarnapp.covidcertificate.common.exception.TestCertificateException.ErrorCode.PKR_404
import de.rki.coronawarnapp.covidcertificate.common.exception.TestCertificateException.ErrorCode.PKR_409
import de.rki.coronawarnapp.covidcertificate.common.exception.TestCertificateException.ErrorCode.PKR_500
import de.rki.coronawarnapp.covidcertificate.common.exception.TestCertificateException.ErrorCode.PKR_FAILED
import de.rki.coronawarnapp.covidcertificate.common.exception.TestCertificateException.ErrorCode.PKR_NO_NETWORK
import de.rki.coronawarnapp.covidcertificate.test.core.server.TestCertificateApiV1.ComponentsResponse.Reason.INTERNAL
import de.rki.coronawarnapp.covidcertificate.test.core.server.TestCertificateApiV1.ComponentsResponse.Reason.LAB_INVALID_RESPONSE
import de.rki.coronawarnapp.covidcertificate.test.core.server.TestCertificateApiV1.ComponentsResponse.Reason.SIGNING_CLIENT_ERROR
import de.rki.coronawarnapp.covidcertificate.test.core.server.TestCertificateApiV1.ComponentsResponse.Reason.SIGNING_SERVER_ERROR
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.encryption.rsa.RSAKey
import de.rki.coronawarnapp.util.network.NetworkStateProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@Reusable
class TestCertificateServer @Inject constructor(
    private val dccApi: Lazy<TestCertificateApiV1>,
    private val dispatcherProvider: DispatcherProvider,
    private val networkStateProvider: NetworkStateProvider
) {

    private val api: TestCertificateApiV1
        get() = dccApi.get()

    @Throws(TestCertificateException::class)
    suspend fun registerPublicKeyForTest(
        testRegistrationToken: RegistrationToken,
        publicKey: RSAKey.Public,
    ): Unit = withContext(dispatcherProvider.IO) {
        Timber.tag(TAG).v("registerPublicKeyForTest(token=%s, key=%s)", testRegistrationToken, publicKey)
        if (!isInternetAvailable()) {
            throw TestCertificateException(PKR_NO_NETWORK)
        }
        try {
            val response = api.sendPublicKey(
                requestBody = TestCertificateApiV1.PublicKeyUploadRequest(
                    registrationToken = testRegistrationToken,
                    publicKey = publicKey.base64
                )
            )
            when (response.code()) {
                400 -> throw TestCertificateException(PKR_400)
                403 -> throw TestCertificateException(PKR_403)
                404 -> throw TestCertificateException(PKR_404)
                409 -> throw TestCertificateException(PKR_409)
                500 -> throw TestCertificateException(PKR_500)
            }
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "registerPublicKeyForTest failed")
            throw when (e) {
                is TestCertificateException -> e
                else -> TestCertificateException(PKR_FAILED, e)
            }
        }
    }

    @Throws(TestCertificateException::class)
    suspend fun requestCertificateForTest(
        testRegistrationToken: RegistrationToken,
    ): TestCertificateComponents = withContext(dispatcherProvider.IO) {
        Timber.tag(TAG).v("requestCertificateForTest(token=%s)", testRegistrationToken)
        if (!isInternetAvailable()) {
            throw TestCertificateException(DCC_COMP_NO_NETWORK)
        }
        val response = api.getComponents(
            requestBody = TestCertificateApiV1.ComponentsRequest(testRegistrationToken)
        )
        when (response.code()) {
            202 -> throw TestCertificateException(DCC_COMP_202)
            400 -> throw TestCertificateException(DCC_COMP_400)
            404 -> throw TestCertificateException(DCC_COMP_404)
            410 -> throw TestCertificateException(DCC_COMP_410)
            412 -> throw TestCertificateException(DCC_COMP_412)
            500 -> when (response.body()?.errorReason) {
                INTERNAL.errorString -> throw TestCertificateException(DCC_COMP_500)
                SIGNING_CLIENT_ERROR.errorString ->
                    throw TestCertificateException(DCC_COMP_500_SIGNING_CLIENT_ERROR)
                SIGNING_SERVER_ERROR.errorString ->
                    throw TestCertificateException(DCC_COMP_500_SIGNING_SERVER_ERROR)
                LAB_INVALID_RESPONSE.errorString ->
                    throw TestCertificateException(DCC_COMP_500_LAB_INVALID_RESPONSE)
            }
        }
        val result = response.body()!! // throw exception?
        TestCertificateComponents(
            dataEncryptionKeyBase64 = result.dek!!,
            encryptedCoseTestCertificateBase64 = result.dcc!!
        )
    }

    private suspend fun isInternetAvailable() = networkStateProvider.networkState.first().isInternetAvailable

    companion object {
        private const val TAG = "CovidCertificateServer"
    }
}
