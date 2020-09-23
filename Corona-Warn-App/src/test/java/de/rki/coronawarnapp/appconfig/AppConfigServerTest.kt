package de.rki.coronawarnapp.appconfig

import dagger.Lazy
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
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
import java.io.IOException

class AppConfigServerTest : BaseIOTest() {

    @MockK lateinit var api: AppConfigApiV1
    @MockK lateinit var verificationKeys: VerificationKeys
    @MockK lateinit var appConfigStorage: AppConfigStorage

    private val testDir = File(IO_TEST_BASEDIR, this::class.simpleName!!)

    private val defaultHomeCountry = LocationCode("DE")

    private var mockConfigStorage: ByteArray? = null

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        testDir.mkdirs()
        testDir.exists() shouldBe true

        every { appConfigStorage.appConfigRaw } answers { mockConfigStorage }
        every { appConfigStorage.appConfigRaw = any() } answers { mockConfigStorage = arg(0) }
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
        homeCountry = homeCountry,
        configStorage = appConfigStorage
    )

    @Test
    fun `application config download`() {
        coEvery { api.getApplicationConfiguration("DE") } returns APPCONFIG_BUNDLE.toResponseBody()

        every { verificationKeys.hasInvalidSignature(any(), any()) } returns false

        val downloadServer = createDownloadServer()

        runBlocking {
            val rawConfig = downloadServer.downloadAppConfig()
            rawConfig shouldBe APPCONFIG_RAW.toByteArray()
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
        coEvery { api.getApplicationConfiguration("DE") } returns APPCONFIG_BUNDLE
            .toResponseBody()

        every { verificationKeys.hasInvalidSignature(any(), any()) } returns true

        val downloadServer = createDownloadServer()

        runBlocking {
            shouldThrow<ApplicationConfigurationCorruptException> {
                downloadServer.downloadAppConfig()
            }
        }
    }

    @Test
    fun `successful download stores new config`() {
        coEvery { api.getApplicationConfiguration("DE") } returns APPCONFIG_BUNDLE
            .toResponseBody()

        every { verificationKeys.hasInvalidSignature(any(), any()) } returns false

        val downloadServer = createDownloadServer()

        runBlocking {
            downloadServer.getAppConfig()

            mockConfigStorage shouldBe APPCONFIG_RAW.toByteArray()
            verify { appConfigStorage.appConfigRaw = APPCONFIG_RAW.toByteArray() }
        }
    }

    @Test
    fun `failed download doesn't overwrite valid config`() {
        mockConfigStorage = APPCONFIG_RAW.toByteArray()
        coEvery { api.getApplicationConfiguration("DE") } throws IOException()

        every { verificationKeys.hasInvalidSignature(any(), any()) } returns false

        runBlocking {
            createDownloadServer().getAppConfig()
        }

        verify(exactly = 0) { appConfigStorage.appConfigRaw = any() }
        mockConfigStorage shouldBe APPCONFIG_RAW.toByteArray()
    }

    @Test
    fun `failed verification doesn't overwrite valid config`() {
        mockConfigStorage = APPCONFIG_RAW.toByteArray()
        coEvery { api.getApplicationConfiguration("DE") } returns APPCONFIG_BUNDLE
            .toResponseBody()

        every { verificationKeys.hasInvalidSignature(any(), any()) } returns true

        runBlocking {
            createDownloadServer().getAppConfig()
        }

        verify(exactly = 0) { appConfigStorage.appConfigRaw = any() }
        mockConfigStorage shouldBe APPCONFIG_RAW.toByteArray()
    }

    @Test
    fun `fallback to last config if verification fails`() {
        mockConfigStorage = APPCONFIG_RAW.toByteArray()

        coEvery { api.getApplicationConfiguration("DE") } returns "123ABC".decodeHex()
            .toResponseBody()

        every { verificationKeys.hasInvalidSignature(any(), any()) } throws Exception()
        runBlocking {
            createDownloadServer().getAppConfig().minRiskScore shouldBe 11
        }

        every { verificationKeys.hasInvalidSignature(any(), any()) } returns true
        runBlocking {
            createDownloadServer().getAppConfig().minRiskScore shouldBe 11
        }
    }

    @Test
    fun `fallback to last config if download fails`() {
        mockConfigStorage = APPCONFIG_RAW.toByteArray()

        coEvery { api.getApplicationConfiguration("DE") } throws Exception()

        every { verificationKeys.hasInvalidSignature(any(), any()) } returns false

        runBlocking {
            createDownloadServer().getAppConfig().minRiskScore shouldBe 11
        }
    }

    companion object {
        private val APPCONFIG_BUNDLE =
            ("504b0304140008080800856b22510000000000000000000000000a0000006578706f72742e62696ee3e016" +
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
                "0200020070000000ae0100000000").decodeHex()
        private val APPCONFIG_RAW =
            ("080b124d0a230a034c4f57180f221a68747470733a2f2f777777" +
                "2e636f726f6e617761726e2e6170700a260a0448494748100f1848221a68747470733a2f2f7777772e636f7" +
                "26f6e617761726e2e6170701a640a10080110021803200428053006380740081100000000000049401a0a20" +
                "0128013001380140012100000000000049402a1008051005180520052805300538054005310000000000003" +
                "4403a0e1001180120012801300138014001410000000000004940221c0a040837103f121209000000000000" +
                "f03f11000000000000e03f20192a1a0a0a0a041008180212021005120c0a0408011804120408011804").decodeHex()
    }
}
