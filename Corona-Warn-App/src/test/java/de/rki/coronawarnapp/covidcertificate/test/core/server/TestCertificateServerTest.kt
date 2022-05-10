package de.rki.coronawarnapp.covidcertificate.test.core.server

import de.rki.coronawarnapp.covidcertificate.common.exception.TestCertificateException
import de.rki.coronawarnapp.exception.http.ConflictException
import de.rki.coronawarnapp.util.encryption.rsa.RSAKeyPairGenerator
import de.rki.coronawarnapp.util.network.NetworkStateProvider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider

class TestCertificateServerTest : BaseTest() {

    @MockK lateinit var dccApi: TestCertificateApiV1
    @MockK lateinit var networkStateProvider: NetworkStateProvider
    @MockK lateinit var componentsResponse: TestCertificateApiV1.ComponentsResponse
    private val networkState = mockk<NetworkStateProvider.State>().apply {
        every { isInternetAvailable } returns true
    }

    private val keyPair = RSAKeyPairGenerator().generate()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        dccApi.apply {
            coEvery { sendPublicKey(any()) } returns mockk()
            coEvery { getComponents(any()) } returns Response.success(componentsResponse)
        }

        every { networkStateProvider.networkState } returns flow { emit(networkState) }
    }

    private fun createInstance() = TestCertificateServer(
        dccApi = { dccApi },
        dispatcherProvider = TestDispatcherProvider(),
        networkStateProvider = networkStateProvider,
    )

    @Test
    fun `happy path - successful registration`() = runTest {
        createInstance().registerPublicKeyForTest(
            testRegistrationToken = "token",
            publicKey = keyPair.publicKey
        )

        coVerify {
            dccApi.sendPublicKey(
                requestBody = TestCertificateApiV1.PublicKeyUploadRequest(
                    registrationToken = "token",
                    publicKey = keyPair.publicKey.base64
                )
            )
        }
    }

    @Test
    fun `error mapping - no network`() = runTest {
        every { networkState.isInternetAvailable } returns false

        shouldThrow<TestCertificateException> {
            createInstance().registerPublicKeyForTest(
                testRegistrationToken = "token",
                publicKey = keyPair.publicKey
            )
        }.errorCode shouldBe TestCertificateException.ErrorCode.PKR_NO_NETWORK

        coVerify { dccApi wasNot Called }
    }

    @Test
    fun `error mapping - http 409`() = runTest {
        coEvery { dccApi.sendPublicKey(any()) } throws ConflictException("test")
        shouldThrow<TestCertificateException> {
            createInstance().registerPublicKeyForTest(
                testRegistrationToken = "token",
                publicKey = keyPair.publicKey
            )
        }.errorCode shouldBe TestCertificateException.ErrorCode.PKR_409

        coVerify {
            dccApi.sendPublicKey(
                requestBody = TestCertificateApiV1.PublicKeyUploadRequest(
                    registrationToken = "token",
                    publicKey = keyPair.publicKey.base64
                )
            )
        }
    }
}
