package de.rki.coronawarnapp.eventregistration.events

import de.rki.coronawarnapp.eventregistration.checkins.qrcode.toTraceLocation
import de.rki.coronawarnapp.eventregistration.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.security.SecureRandom

internal class TraceLocationCreatorTest : BaseTest() {

    @MockK lateinit var repository: TraceLocationRepository
    @RelaxedMockK lateinit var secureRandom: SecureRandom

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    private fun createInstance() = TraceLocationCreator(repository, secureRandom)

    @Test
    fun `createTraceLocation() should return traceLocation and store it in repository when everything works fine`() =
        runBlockingTest {

            val userInput = TraceLocationUserInput(
                type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_PRIVATE_EVENT,
                description = "Top Secret Private Event",
                address = "top secret address",
                startDate = Instant.parse("2020-01-01T14:00:00.000Z"),
                endDate = Instant.parse("2020-01-01T18:00:00.000Z"),
                defaultCheckInLengthInMinutes = 180
            )

            val expectedTraceLocation = userInput.toTraceLocation(secureRandom)

            coEvery { repository.addTraceLocation(any()) } returns expectedTraceLocation

            val actualTraceLocation = createInstance().createTraceLocation(userInput)

            actualTraceLocation shouldBe expectedTraceLocation

            coVerify(exactly = 1) { repository.addTraceLocation(expectedTraceLocation) }
        }
}
