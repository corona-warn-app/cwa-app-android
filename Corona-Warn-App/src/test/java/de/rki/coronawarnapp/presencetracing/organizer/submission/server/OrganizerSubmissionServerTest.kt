package de.rki.coronawarnapp.presencetracing.organizer.submission.server

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.PlausibleDeniabilityParametersContainer
import de.rki.coronawarnapp.appconfig.PresenceTracingConfigContainer
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.exception.http.CwaUnknownHostException
import de.rki.coronawarnapp.exception.http.InternalServerErrorException
import de.rki.coronawarnapp.exception.http.NetworkConnectTimeoutException
import de.rki.coronawarnapp.exception.http.UnauthorizedException
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
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import java.security.SecureRandom
import kotlin.random.asKotlinRandom

@Suppress("MaxLineLength")
internal class OrganizerSubmissionServerTest : BaseTest() {

    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var organizerSubmissionApiV1: OrganizerSubmissionApiV1

    private val unencryptedCheckIn = CheckInOuterClass.CheckIn.getDefaultInstance()
    private val encryptedCheckIn = CheckInOuterClass.CheckInProtectedReport.getDefaultInstance()

    private val checkInsReport = CheckInsReport(
        unencryptedCheckIns = listOf(unencryptedCheckIn),
        encryptedCheckIns = listOf(encryptedCheckIn)
    )

    private fun createServer(appConfigProvider: AppConfigProvider, organizerSubmissionApiV1: OrganizerSubmissionApiV1) = OrganizerSubmissionServer(
        paddingTool = PaddingTool(SecureRandom.getInstanceStrong().asKotlinRandom()),
        dispatcherProvider = TestDispatcherProvider(),
        appConfigProvider = appConfigProvider,
        organizerSubmissionApiV1Lazy = { organizerSubmissionApiV1 }
    )

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
        }
    }

    @Test
    fun submit() = runTest {
        createServer(appConfigProvider, organizerSubmissionApiV1).submit("uploadTan", checkInsReport)
    }

    @Test
    fun `forwards exceptions`() = runTest {
        val server = createServer(appConfigProvider, organizerSubmissionApiV1)
        val errorDetails = "errorDetails"
        val uploadTan = "uploadTan"

        coEvery { organizerSubmissionApiV1.submitCheckInsOnBehalf(any(), any()) } throws BadRequestException(errorDetails)

        assertThrows<BadRequestException> {
            server.submit(uploadTan, checkInsReport)
        }

        coEvery { organizerSubmissionApiV1.submitCheckInsOnBehalf(any(), any()) } throws UnauthorizedException(errorDetails)

        assertThrows<UnauthorizedException> {
            server.submit(uploadTan, checkInsReport)
        }

        coEvery { organizerSubmissionApiV1.submitCheckInsOnBehalf(any(), any()) } throws InternalServerErrorException(errorDetails)

        assertThrows<InternalServerErrorException> {
            server.submit(uploadTan, checkInsReport)
        }

        coEvery { organizerSubmissionApiV1.submitCheckInsOnBehalf(any(), any()) } throws NetworkConnectTimeoutException(errorDetails)

        assertThrows<NetworkConnectTimeoutException> {
            server.submit(uploadTan, checkInsReport)
        }

        coEvery { organizerSubmissionApiV1.submitCheckInsOnBehalf(any(), any()) } throws CwaUnknownHostException(errorDetails, null)

        assertThrows<CwaUnknownHostException> {
            server.submit(uploadTan, checkInsReport)
        }
    }
}
