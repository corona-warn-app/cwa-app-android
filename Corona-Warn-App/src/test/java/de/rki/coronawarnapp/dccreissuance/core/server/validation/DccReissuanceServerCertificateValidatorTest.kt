package de.rki.coronawarnapp.dccreissuance.core.server.validation

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.internal.ApplicationConfigurationInvalidException
import de.rki.coronawarnapp.appconfig.mapping.CovidCertificateConfigMapper
import de.rki.coronawarnapp.dccreissuance.core.error.DccReissuanceException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.toByteString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.security.PublicKey
import java.security.cert.Certificate

class DccReissuanceServerCertificateValidatorTest : BaseTest() {

    private val publicKeyEncodedBase64 =
        "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA9UPAy/WbQh/4QaEY18LrEffsbFZQlCxtLpMeXRKGkUsnP21eEJtZIkoGhPAubKNV0xtHmd/AN4vYcOMLdRLwARjQ0uz2MC1UgOStClwD/Aeq8oIvawYgrRKrY5WoiN8kqEONAGecTsHRv/ypDEy6OeZATGQktyFGnuBzGdObbCX97yOYEw1BslSNlO0JC7B7p9by/xPR/1Efa38z6fkXam0lwqKBZ0APVJUkQ/VUQbnPAqgMTGmHMRDf7Mq+nHtrV4zyymwhOOiFrbcgYZ096LO26/+R2GnCPx2p79v4ZJ0/aR/2FdFfnJSqpfppxW54UvxFgRE5/YMqGDPJw7lrT91LoGfR217YJkyveD4g7HOnQaTq7NaOyFURdgDxhngVPgLwNCaW1xV60AvMr3FFCdXd5+Vl4G79bvDPxkwjS0bZlfo6PV2CUVotfvTos4N11/iaLTYZRohdJmwHdYeS2YPHNvEIbMwHA7AZubmIuHjnm3SBRK1GSy/Ats1AG3IMYVdZ6WnD/xfiXMX0knygn+eLx7H7o4t5yGY1YsNMpvGKruVTXR/g1euofK4m/ZWEbwbsPDXjYE4/d6bUGWZc7vUc/X0wW0iMxHVE8Ra4xAhBDc/p/gWB2kN6NzInFTVBERyw5WozDwOW+/8qsBtQYoNPK+I8iao6uEISLOcCXr8CAwEAAQ=="
    private val publicKeyEncoded: ByteArray by lazy { publicKeyEncodedBase64.decodeBase64()!!.toByteArray() }

    private val mockedPublicKey: PublicKey = mockk {
        every { encoded } returns publicKeyEncoded
    }

    private val serverCert: Certificate = mockk {
        every { publicKey } returns mockedPublicKey
    }

    @MockK
    lateinit var appConfigProvider: AppConfigProvider

    private val instance: DccReissuanceServerCertificateValidator
        get() = DccReissuanceServerCertificateValidator(appConfigProvider = appConfigProvider)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery { appConfigProvider.getAppConfig() } returns mockk {
            every { covidCertificateParameters } returns CovidCertificateConfigMapper.CovidCertificateConfigContainer(
                reissueServicePublicKeyDigest = publicKeyEncoded.toByteString().sha256()
            )
        }
    }

    @Test
    fun `happy path`() = runBlockingTest {
        instance.checkCertificateChain(certificateChain = listOf(serverCert))
    }

    @Test
    fun `not matching hashes lead to DCC_RI_PIN_MISMATCH`() = runBlockingTest {
        every { mockedPublicKey.encoded } returns "not matching hash".toByteArray()

        shouldThrow<DccReissuanceException> {
            instance.checkCertificateChain(certificateChain = listOf(serverCert))
        }.errorCode shouldBe DccReissuanceException.ErrorCode.DCC_RI_PIN_MISMATCH
    }

    @Test
    fun `maps unspecified errors to DCC_RI_PIN_MISMATCH`() = runBlockingTest {
        coEvery { appConfigProvider.getAppConfig() } throws ApplicationConfigurationInvalidException(
            message = "Test error"
        )

        shouldThrow<DccReissuanceException> { instance.checkCertificateChain(certificateChain = listOf(serverCert)) }
            .also {
                it.errorCode shouldBe DccReissuanceException.ErrorCode.DCC_RI_PIN_MISMATCH
                it.cause should beInstanceOf(ApplicationConfigurationInvalidException::class)
            }

        shouldThrow<DccReissuanceException> { instance.checkCertificateChain(certificateChain = emptyList()) }.also {
            it.errorCode shouldBe DccReissuanceException.ErrorCode.DCC_RI_PIN_MISMATCH
            it.cause should beInstanceOf(NoSuchElementException::class)
        }
    }
}
