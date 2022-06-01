package de.rki.coronawarnapp.submission.server

import android.content.Context
import com.google.protobuf.ByteString
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.PlausibleDeniabilityParametersContainer
import de.rki.coronawarnapp.appconfig.PresenceTracingConfigContainer
import de.rki.coronawarnapp.http.HttpModule
import de.rki.coronawarnapp.server.protocols.external.exposurenotification.TemporaryExposureKeyExportOuterClass
import de.rki.coronawarnapp.server.protocols.internal.SubmissionPayloadOuterClass
import de.rki.coronawarnapp.server.protocols.internal.SubmissionPayloadOuterClass.SubmissionPayload.SubmissionType
import de.rki.coronawarnapp.server.protocols.internal.pt.CheckInOuterClass
import de.rki.coronawarnapp.server.protocols.internal.v2.PresenceTracingParametersOuterClass.PresenceTracingPlausibleDeniabilityParameters.NumberOfFakeCheckInsFunctionParameters
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import de.rki.coronawarnapp.submission.SubmissionModule
import de.rki.coronawarnapp.util.PaddingTool
import de.rki.coronawarnapp.util.headerSizeIgnoringContentLength
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ConnectionSpec
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import testhelpers.BaseTest
import java.io.File
import java.security.SecureRandom
import kotlin.random.asKotlinRandom

class SubmissionServerTest : BaseTest() {
    @MockK lateinit var submissionApi: SubmissionApiV1
    @MockK lateinit var context: Context
    @MockK lateinit var appConfigProvider: AppConfigProvider

    private lateinit var webServer: MockWebServer
    private lateinit var serverAddress: String

    private val testDir = File(BaseIOTest.IO_TEST_BASEDIR, this::class.java.simpleName)
    private val cacheDir = File(testDir, "cache")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        testDir.mkdirs()
        testDir.exists() shouldBe true
        every { context.cacheDir } returns cacheDir
        coEvery { appConfigProvider.getAppConfig() } returns mockk<ConfigData>().apply {
            every { presenceTracing } returns PresenceTracingConfigContainer(
                plausibleDeniabilityParameters = PlausibleDeniabilityParametersContainer()
            )
        }

        webServer = MockWebServer()
        webServer.start()
        serverAddress = "http://${webServer.hostName}:${webServer.port}"
    }

    @AfterEach
    fun teardown() {
        webServer.shutdown()
        testDir.deleteRecursively()
    }

    private fun createServer(
        customApi: SubmissionApiV1 = submissionApi
    ) = SubmissionServer(
        submissionApi = { customApi },
        appConfigProvider = appConfigProvider,
        paddingTool = PaddingTool(
            sourceFast = SecureRandom().asKotlinRandom(),
        ),
    )

    @Test
    fun `genuine submission - empty checkInPadding`(): Unit = runTest {
        val testKeyData = ByteString.copyFrom("TestKeyDataGoogle", Charsets.UTF_8)
        val unencryptedCheckIn = CheckInOuterClass.CheckIn.newBuilder()
            .setEndIntervalNumber(0)
            .setStartIntervalNumber(0)
            .build()

        val encryptedCheckIn = CheckInOuterClass.CheckInProtectedReport.newBuilder()
            .build()

        val server = createServer()
        coEvery { submissionApi.submitPayload(any(), any(), any(), any()) } answers {
            arg<String>(0) shouldBe "testAuthCode"
            arg<String>(1) shouldBe "0"
            arg<String>(2) shouldBe ""
            arg<SubmissionPayloadOuterClass.SubmissionPayload>(3).apply {
                keysList.single().keyData shouldBe testKeyData
                checkInsOrBuilderList.size shouldBe 1
                checkInsOrBuilderList[0] shouldBe unencryptedCheckIn

                checkInProtectedReportsList.size shouldBe 1
                checkInProtectedReportsList[0] shouldBe encryptedCheckIn

                requestPadding.size() shouldBe 392
                hasConsentToFederation() shouldBe true
                visitedCountriesList shouldBe listOf("DE")
            }
            Unit
        }

        val googleKeyList = TemporaryExposureKeyExportOuterClass.TemporaryExposureKey
            .newBuilder()
            .setKeyData(testKeyData)
            .build()

        val submissionData = SubmissionServer.SubmissionData(
            authCode = "testAuthCode",
            keyList = listOf(googleKeyList),
            consentToFederation = true,
            visitedCountries = listOf("DE"),
            unencryptedCheckIns = listOf(unencryptedCheckIn),
            encryptedCheckIns = listOf(encryptedCheckIn),
            submissionType = SubmissionType.SUBMISSION_TYPE_PCR_TEST
        )
        server.submitPayload(submissionData)

        coVerify { submissionApi.submitPayload(any(), any(), any(), any()) }
    }

    @Test
    fun `genuine submission - non empty checkInPadding`(): Unit = runTest {
        coEvery { appConfigProvider.getAppConfig() } returns mockk<ConfigData>().apply {
            every { presenceTracing } returns PresenceTracingConfigContainer(
                plausibleDeniabilityParameters = PlausibleDeniabilityParametersContainer(
                    checkInSizesInBytes = listOf(10),
                    probabilityToFakeCheckInsIfSomeCheckIns = 1.0,
                    probabilityToFakeCheckInsIfNoCheckIns = 1.0,
                    numberOfFakeCheckInsFunctionParameters = listOf(
                        NumberOfFakeCheckInsFunctionParameters.newBuilder()
                            .setRandomNumberRange(
                                RiskCalculationParametersOuterClass.Range.newBuilder()
                                    .setMax(9999.0)
                                    .setMin(-9999.0)
                            )
                            .setP(100.0)
                            .setQ(1.4)
                            .setR(-1.0)
                            .setS(0.8)
                            .setT(-1.5)
                            .setU(2.0)
                            .setA(0.0)
                            .setB(0.0)
                            .setC(0.0)
                            .build()
                    )
                )
            )
        }
        val testKeyData = ByteString.copyFrom("TestKeyDataGoogle", Charsets.UTF_8)
        val unencryptedCheckIn = CheckInOuterClass.CheckIn.newBuilder()
            .setEndIntervalNumber(0)
            .setStartIntervalNumber(0)
            .build()

        val encryptedCheckIn = CheckInOuterClass.CheckInProtectedReport.newBuilder()
            .build()

        val server = createServer()
        coEvery { submissionApi.submitPayload(any(), any(), any(), any()) } answers {
            arg<String>(0) shouldBe "testAuthCode"
            arg<String>(1) shouldBe "0"
            arg<String>(2) shouldBe ""
            arg<SubmissionPayloadOuterClass.SubmissionPayload>(3).apply {
                keysList.single().keyData shouldBe testKeyData

                checkInsOrBuilderList.size shouldBe 1
                checkInsOrBuilderList[0] shouldBe unencryptedCheckIn

                checkInProtectedReportsList.size shouldBe 1
                checkInProtectedReportsList[0] shouldBe encryptedCheckIn

                // CheckInPadding length is random > is used to check it exists
                requestPadding.size() shouldBeGreaterThan 392
                hasConsentToFederation() shouldBe true
                visitedCountriesList shouldBe listOf("DE")
            }
            Unit
        }

        val googleKeyList = TemporaryExposureKeyExportOuterClass.TemporaryExposureKey
            .newBuilder()
            .setKeyData(testKeyData)
            .build()

        val submissionData = SubmissionServer.SubmissionData(
            authCode = "testAuthCode",
            keyList = listOf(googleKeyList),
            consentToFederation = true,
            visitedCountries = listOf("DE"),
            unencryptedCheckIns = listOf(unencryptedCheckIn),
            encryptedCheckIns = listOf(encryptedCheckIn),
            submissionType = SubmissionType.SUBMISSION_TYPE_PCR_TEST
        )
        server.submitPayload(submissionData)

        coVerify { submissionApi.submitPayload(any(), any(), any(), any()) }
    }

    @Test
    fun `fake submission`(): Unit = runTest {
        val server = createServer()
        coEvery { submissionApi.submitPayload(any(), any(), any(), any()) } answers {
            arg<String>(0) shouldBe "" // cwa-authorization
            arg<String>(1) shouldBe "1" // cwa-fake
            arg<String>(2).length shouldBe 36 // cwa-header-padding
            arg<SubmissionPayloadOuterClass.SubmissionPayload>(3).apply {
                keysList.size shouldBe 0
                checkInsOrBuilderList.size shouldBe 0
                checkInProtectedReportsList.size shouldBe 0
                requestPadding.size() shouldBe 420
                hasConsentToFederation() shouldBe false
                visitedCountriesList shouldBe emptyList()
            }
            Unit
        }

        server.submitFakePayload()

        coVerify { submissionApi.submitPayload(any(), any(), any(), any()) }
    }

    private fun createRealApi(): SubmissionApiV1 {
        val httpModule = HttpModule()
        val defaultHttpClient = httpModule.defaultHttpClient()

        return SubmissionModule.let {
            val downloadHttpClient = it.cdnHttpClient(
                defaultHttpClient,
                listOf(ConnectionSpec.CLEARTEXT, ConnectionSpec.MODERN_TLS)
            )
            it.provideSubmissionApi(
                context = context,
                client = downloadHttpClient,
                url = serverAddress,
                gsonConverterFactory = httpModule.provideGSONConverter(),
                protoConverterFactory = httpModule.provideProtoConverter()
            )
        }
    }

    @Test
    fun allRequestHaveSameFootprintForPlausibleDeniability(): Unit = runTest {
        val server = createServer(createRealApi())

        val testKeyData = ByteString.copyFrom("TestKeyDataGoogle", Charsets.UTF_8)
        val googleKeyList = TemporaryExposureKeyExportOuterClass.TemporaryExposureKey
            .newBuilder()
            .setKeyData(testKeyData)
            .build()
        val submissionData = SubmissionServer.SubmissionData(
            authCode = "39ec4930-7a1f-4d5d-921f-bfad3b6f1269",
            keyList = listOf(googleKeyList),
            consentToFederation = true,
            visitedCountries = listOf("DE"),
            unencryptedCheckIns = emptyList(),
            encryptedCheckIns = emptyList(),
            submissionType = SubmissionType.SUBMISSION_TYPE_PCR_TEST
        )
        webServer.enqueue(MockResponse().setBody("{}"))
        server.submitPayload(submissionData)

        webServer.enqueue(MockResponse().setBody("{}"))
        server.submitFakePayload()

        val requests = listOf(
            webServer.takeRequest(),
            webServer.takeRequest()
        )

        // ensure all request have same size (header & body)
        requests.zipWithNext().forEach { (a, b) ->
            a.headerSizeIgnoringContentLength() shouldBe b.headerSizeIgnoringContentLength()
        }
    }
}
