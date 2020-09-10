package de.rki.coronawarnapp.diagnosiskeys.server

import dagger.Lazy
import de.rki.coronawarnapp.exception.ApplicationConfigurationCorruptException
import de.rki.coronawarnapp.exception.ApplicationConfigurationInvalidException
import de.rki.coronawarnapp.util.security.VerificationKeys
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.ByteString.Companion.decodeHex
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File

class AppConfigServerTest : BaseIOTest() {

    @MockK
    lateinit var api: AppConfigApiV1

    @MockK
    lateinit var verificationKeys: VerificationKeys

    private val testDir = File(IO_TEST_BASEDIR, this::class.simpleName!!)

    private val defaultHomeCountry = LocationCode("DE")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        testDir.mkdirs()
        testDir.exists() shouldBe true
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
        testDir.deleteRecursively()
    }

    private fun createDownloadServer(
        homeCountry: LocationCode = defaultHomeCountry
    ) = AppConfigServer(
        appConfigAPI = Lazy { api },
        verificationKeys = verificationKeys,
        homeCountry = homeCountry
    )

    @Test
    fun `application config download`() {
        coEvery { api.getApplicationConfiguration("DE") } returns APPCONFIG_HEX.decodeHex()
            .toResponseBody()

        every { verificationKeys.hasInvalidSignature(any(), any()) } returns false

        val downloadServer = createDownloadServer()

        runBlocking {
            val config = downloadServer.downloadAppConfig()
            config.apply {
                // We just care here that it's non default values, i.e. conversion worked
                minRiskScore shouldBe 11
                appVersion.android.latest.major shouldBe 1
                appVersion.android.latest.minor shouldBe 0
                appVersion.android.latest.patch shouldBe 4

            }
        }

        verify(exactly = 1) { verificationKeys.hasInvalidSignature(any(), any()) }
    }

    @Test
    fun `application config data is faulty`() {
        coEvery { api.getApplicationConfiguration("DE") } returns "123ABC".decodeHex()
            .toResponseBody()

        every { verificationKeys.hasInvalidSignature(any(), any()) } returns false

        val downloadServer = createDownloadServer()

        runBlocking {
            shouldThrow<ApplicationConfigurationInvalidException> {
                downloadServer.downloadAppConfig()
            }
        }
    }

    @Test
    fun `application config verification fails`() {
        coEvery { api.getApplicationConfiguration("DE") } returns APPCONFIG_HEX.decodeHex()
            .toResponseBody()

        every { verificationKeys.hasInvalidSignature(any(), any()) } returns true

        val downloadServer = createDownloadServer()

        runBlocking {
            shouldThrow<ApplicationConfigurationCorruptException> {
                downloadServer.downloadAppConfig()
            }
        }
    }

    companion object {
        private const val APPCONFIG_HEX =
            "504b0304140008080800856b22510000000000000000000000000a0000006578706f72742e62696ee3e016" +
                    "f2e552e662f6f10f97e05792ca28292928b6d2d72f2f2fd74bce2fcacf4b2c4f2ccad34b2c28e0" +
                    "52e362f1f074f710e097f0c0a74e2a854b80835180498259814583d580cd82dd814390010c3c1d" +
                    "a4b8141835180d182d181d181561825a021cac02ac12ac0aac40f5ac16ac0eac86102913072b3e" +
                    "01460946841e47981e25192e160e73017b21214e88d0077ba8250fec1524b5a4b8b8b858043824" +
                    "98849804588578806a19255884c02400504b0708df2c788daf000000f1000000504b0304140008" +
                    "080800856b22510000000000000000000000000a0000006578706f72742e736967018a0075ff0a" +
                    "87010a380a1864652e726b692e636f726f6e617761726e6170702d6465761a0276312203323632" +
                    "2a13312e322e3834302e31303034352e342e332e321001180122473045022100cf32ff24ea18a1" +
                    "ffcc7ff4c9fe8d1808cecbc5a37e3e1d4c9ce682120450958c022064bf124b6973a9b510a43d47" +
                    "9ff93e0ef97a5b893c7af4abc4a8d399969cd8a0504b070813c517c68f0000008a000000504b01" +
                    "021400140008080800856b2251df2c788daf000000f10000000a00000000000000000000000000" +
                    "000000006578706f72742e62696e504b01021400140008080800856b225113c517c68f0000008a" +
                    "0000000a00000000000000000000000000e70000006578706f72742e736967504b050600000000" +
                    "0200020070000000ae0100000000"
    }
}
