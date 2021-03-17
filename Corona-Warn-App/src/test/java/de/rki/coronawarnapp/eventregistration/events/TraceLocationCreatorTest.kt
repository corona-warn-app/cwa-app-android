package de.rki.coronawarnapp.eventregistration.events

import com.google.protobuf.ByteString
import dagger.Lazy
import de.rki.coronawarnapp.eventregistration.events.server.TraceLocationServer
import de.rki.coronawarnapp.eventregistration.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import de.rki.coronawarnapp.util.security.SignatureValidation
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class TraceLocationCreatorTest : BaseTest() {

    @MockK lateinit var api: Lazy<TraceLocationServer>
    @MockK lateinit var repository: TraceLocationRepository
    @MockK lateinit var signatureValidation: SignatureValidation

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    private fun createInstance() = TraceLocationCreator(api, repository, signatureValidation)

    @Test
    fun `createTraceLocation() should return traceLocation and store it in repository when everything works fine`() =
        runBlockingTest {

            val traceLocationToReturn = TraceLocationOuterClass.TraceLocation.newBuilder()
                .setVersion(TRACE_LOCATION_VERSION)
                .setType(TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_PRIVATE_EVENT)
                .setDescription("Top Secret Private Event")
                .setAddress("top secret address")
                .setStartTimestamp(Instant.parse("2020-01-01T14:00:00.000Z").seconds)
                .setEndTimestamp(Instant.parse("2020-01-01T18:00:00.000Z").seconds)
                .setDefaultCheckInLengthInMinutes(180)
                .build()

            val signedTraceLocationToReturn = TraceLocationOuterClass.SignedTraceLocation.newBuilder()
                .setLocation(traceLocationToReturn)
                .setSignature(ByteString.copyFromUtf8("Signature"))
                .build()

            coEvery { api.get().createTraceLocation(any()) } returns signedTraceLocationToReturn
            every { signatureValidation.hasValidSignature(any(), any()) } returns true
            every { repository.addTraceLocation(any()) } just Runs

            val userInput = TraceLocationUserInput(
                type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_PRIVATE_EVENT,
                description = "Top Secret Private Event",
                address = "top secret address",
                startDate = Instant.parse("2020-01-01T14:00:00.000Z"),
                endDate = Instant.parse("2020-01-01T18:00:00.000Z"),
                defaultCheckInLengthInMinutes = 180
            )

            val actualTraceLocation = createInstance().createTraceLocation(userInput)
            val expectedTraceLocation = signedTraceLocationToReturn.toTraceLocation()

            verify(exactly = 1) { repository.addTraceLocation(expectedTraceLocation) }

            actualTraceLocation shouldBe expectedTraceLocation
        }

    // TODO: Add tests for exception handling when exception handling is specified in the TechSpecs
}
