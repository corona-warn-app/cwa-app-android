package de.rki.coronawarnapp.presencetracing.organizer.submission

import de.rki.coronawarnapp.playbook.OrganizerPlaybook
import de.rki.coronawarnapp.presencetracing.checkins.CheckInsReport
import de.rki.coronawarnapp.presencetracing.checkins.OrganizerCheckInsTransformer
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.util.TimeAndDateExtensions.secondsToInstant
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.decodeBase64
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class OrganizerSubmissionRepositoryTest : BaseTest() {

    @MockK lateinit var checkInsTransformer: OrganizerCheckInsTransformer
    @MockK lateinit var organizerPlaybook: OrganizerPlaybook

    private val traceLocation = TraceLocation(
        id = 2,
        type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER,
        description = "Your Birthday Party",
        address = "at your place",
        startDate = 1618740005L.secondsToInstant(),
        endDate = 1618865545L.secondsToInstant(),
        defaultCheckInLengthInMinutes = null,
        cryptographicSeed = OrganizerSubmissionPayloadTest.CRYPTOGRAPHIC_SEED.decodeBase64()!!,
        cnPublicKey = OrganizerSubmissionPayloadTest.PUB_KEY,
        version = TraceLocation.VERSION
    )
    private val organizerSubmissionPayload = OrganizerSubmissionPayload(
        traceLocation = traceLocation,
        startDate = Instant.parse("2021-05-10T11:35:00.000Z"),
        endDate = Instant.parse("2021-05-10T13:00:00.000Z"),
        tan = "TAN_TAN_TAN_TAN"
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { checkInsTransformer.transform(any()) } returns CheckInsReport(
            unencryptedCheckIns = emptyList(),
            encryptedCheckIns = emptyList()
        )

        coEvery { organizerPlaybook.submit(any(), any()) } just Runs
    }

    @Test
    fun `submit - Prepare checkins and then submit`() = runTest {
        organizerSubmissionRepository().submit(organizerSubmissionPayload)

        coVerifySequence {
            checkInsTransformer.transform(any())
            organizerPlaybook.submit(any(), any())
        }
    }

    @Test
    fun `submit - throws Exception when Preparing checkins fails`() = runTest {
        coEvery { checkInsTransformer.transform(any()) } throws Exception()
        shouldThrow<Exception> {
            organizerSubmissionRepository().submit(organizerSubmissionPayload)
        }
    }

    @Test
    fun `submit - throws Exception when submit fails`() = runTest {
        coEvery { organizerPlaybook.submit(any(), any()) } throws Exception()
        shouldThrow<Exception> {
            organizerSubmissionRepository().submit(organizerSubmissionPayload)
        }
    }

    private fun TestScope.organizerSubmissionRepository() = OrganizerSubmissionRepository(
        appScope = this,
        checkInsTransformer = checkInsTransformer,
        organizerPlaybook = organizerPlaybook
    )
}
