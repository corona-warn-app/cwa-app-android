package de.rki.coronawarnapp.presencetracing.organizer.submission.server

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.PlausibleDeniabilityParametersContainer
import de.rki.coronawarnapp.appconfig.PresenceTracingConfigContainer
import de.rki.coronawarnapp.presencetracing.checkins.CheckInsReport
import de.rki.coronawarnapp.server.protocols.internal.SubmissionPayloadOuterClass.SubmissionPayload
import de.rki.coronawarnapp.server.protocols.internal.pt.CheckInOuterClass
import de.rki.coronawarnapp.util.PaddingTool
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import java.security.SecureRandom
import kotlin.random.asKotlinRandom

internal class OrganizerSubmissionServerTest : BaseTest() {

    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var organizerSubmissionApiV1: OrganizerSubmissionApiV1

    private lateinit var organizerSubmissionServer: OrganizerSubmissionServer

    private val unencryptedCheckIn = CheckInOuterClass.CheckIn.getDefaultInstance()
    private val encryptedCheckIn = CheckInOuterClass.CheckInProtectedReport.getDefaultInstance()

    @BeforeEach
    fun setUp() {

        MockKAnnotations.init(this)
        coEvery { appConfigProvider.getAppConfig() } returns mockk<ConfigData>().apply {
            every { presenceTracing } returns PresenceTracingConfigContainer(
                plausibleDeniabilityParameters = PlausibleDeniabilityParametersContainer()
            )
        }

        coEvery { organizerSubmissionApiV1.submitCheckInsOnBehalf(any(), any()) } answers {
            arg<String>(0) shouldBe "uploadTan"
            arg<SubmissionPayload>(1).apply {
                keysList.size shouldBe 0
                checkInsOrBuilderList.size shouldBe 1
                checkInsOrBuilderList[0] shouldBe unencryptedCheckIn

                checkInProtectedReportsList.size shouldBe 1
                checkInProtectedReportsList[0] shouldBe encryptedCheckIn

                consentToFederation shouldBe false
                visitedCountriesList.size shouldBe 0
                requestPadding.size() shouldBe 0

                submissionType shouldBe SubmissionPayload.SubmissionType.SUBMISSION_TYPE_HOST_WARNING
            }

            Response.success("{}".toResponseBody())
        }

        organizerSubmissionServer = OrganizerSubmissionServer(
            paddingTool = PaddingTool(SecureRandom.getInstanceStrong().asKotlinRandom()),
            dispatcherProvider = TestDispatcherProvider(),
            appConfigProvider = appConfigProvider,
            organizerSubmissionApiV1 = organizerSubmissionApiV1
        )
    }

    @Test
    fun submit() = runBlockingTest {
        val checkInsReport = CheckInsReport(
            unencryptedCheckIns = listOf(unencryptedCheckIn),
            encryptedCheckIns = listOf(encryptedCheckIn)
        )
        organizerSubmissionServer.submit("uploadTan", checkInsReport)
    }
}
