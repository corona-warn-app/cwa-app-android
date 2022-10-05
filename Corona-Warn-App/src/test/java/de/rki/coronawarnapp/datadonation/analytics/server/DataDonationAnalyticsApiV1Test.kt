package de.rki.coronawarnapp.datadonation.analytics.server

import de.rki.coronawarnapp.datadonation.analytics.AnalyticsModule
import de.rki.coronawarnapp.http.HttpModule
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaDataRequestAndroid
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpacAndroid
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.util.concurrent.TimeUnit

class DataDonationAnalyticsApiV1Test : BaseTest() {
    private lateinit var webServer: MockWebServer
    private lateinit var serverAddress: String

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        webServer = MockWebServer()
        webServer.start()
        serverAddress = "http://${webServer.hostName}:${webServer.port}"
    }

    @AfterEach
    fun teardown() {
        webServer.shutdown()
    }

    private fun createAPI(): DataDonationAnalyticsApiV1 {
        val httpModule = HttpModule()
        val defaultHttpClient = httpModule.defaultHttpClient()

        return AnalyticsModule.provideAnalyticsSubmissionApi(
            client = defaultHttpClient,
            url = serverAddress,
            gsonConverterFactory = httpModule.provideGSONConverter(),
            protoConverterFactory = httpModule.provideProtoConverter()
        )
    }

    @Test
    fun `test submitAndroidAnalytics`(): Unit = runTest {
        val api = createAPI()

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

        webServer.enqueue(MockResponse().setResponseCode(204))

        api.submitAndroidAnalytics(analyticsRequest)

        webServer.takeRequest(5, TimeUnit.SECONDS)!!.apply {
            path shouldBe "/version/v1/android/dat"
            body.readByteArray() shouldBe analyticsRequest.toByteArray()
        }
    }
}
