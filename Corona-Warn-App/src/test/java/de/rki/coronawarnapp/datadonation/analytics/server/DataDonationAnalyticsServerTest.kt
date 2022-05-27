package de.rki.coronawarnapp.datadonation.analytics.server

import android.content.Context
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaDataRequestAndroid
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpacAndroid
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockWebServer
import java.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response
import testhelpers.BaseIOTest
import testhelpers.BaseTest
import java.io.File

class DataDonationAnalyticsServerTest : BaseTest() {
    @MockK lateinit var donationApi: DataDonationAnalyticsApiV1
    @MockK lateinit var context: Context

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
        customApi: DataDonationAnalyticsApiV1 = donationApi
    ) = DataDonationAnalyticsServer(api = { customApi })

    @Test
    fun `normal submission`(): Unit = runTest {
        val server = createServer()

        val baseTime = Instant.ofEpochMilli(0)

        val metadata = PpaData.ExposureRiskMetadata.newBuilder()
            .setRiskLevel(PpaData.PPARiskLevel.RISK_LEVEL_HIGH)
            .setMostRecentDateAtRiskLevel(baseTime.toEpochMilli())
            .setDateChangedComparedToPreviousSubmission(true)
            .setRiskLevelChangedComparedToPreviousSubmission(true)
            .build()

        val payload = PpaData.PPADataAndroid.newBuilder()
            .addExposureRiskMetadataSet(metadata)
            .build()

        val analyticsRequest = PpaDataRequestAndroid.PPADataRequestAndroid.newBuilder()
            .setPayload(payload)
            .setAuthentication(PpacAndroid.PPACAndroid.getDefaultInstance())
            .build()

        coEvery { donationApi.submitAndroidAnalytics(analyticsRequest) } returns
            Response.success(204, DataDonationAnalyticsApiV1.DataDonationAnalyticsResponse(""))

        server.uploadAnalyticsData(analyticsRequest)

        coVerify { donationApi.submitAndroidAnalytics(analyticsRequest) }
    }
}
