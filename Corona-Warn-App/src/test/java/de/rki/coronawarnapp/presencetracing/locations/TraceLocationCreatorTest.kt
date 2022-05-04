package de.rki.coronawarnapp.presencetracing.locations

import de.rki.coronawarnapp.environment.EnvironmentSetup
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.decodeHex
import okio.ByteString.Companion.toByteString
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.security.SecureRandom
import kotlin.random.Random
import kotlin.random.asKotlinRandom

internal class TraceLocationCreatorTest : BaseTest() {

    @MockK lateinit var repository: TraceLocationRepository
    @RelaxedMockK lateinit var secureRandom: SecureRandom
    @MockK private lateinit var environmentSetup: EnvironmentSetup

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { environmentSetup.crowdNotifierPublicKey } returns "cnPublicKey123"

        every { secureRandom.nextBytes(any()) } answers {
            val byteArray = arg<ByteArray>(0)
            Random(0).nextBytes(byteArray)
        }
    }

    private fun createInstance() = TraceLocationCreator(
        repository = repository,
        randomSource = secureRandom.asKotlinRandom(),
        environmentSetup = environmentSetup
    )

    @Test
    fun `createTraceLocation() should return traceLocation and store it in repository when everything works fine`() =
        runTest {

            val userInput = TraceLocationUserInput(
                type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_PRIVATE_EVENT,
                description = "Top Secret Private Event",
                address = "top secret address",
                startDate = Instant.parse("2020-01-01T14:00:00.000Z"),
                endDate = Instant.parse("2020-01-01T18:00:00.000Z"),
                defaultCheckInLengthInMinutes = 180
            )

            val expectedTraceLocation = userInput.toTraceLocation(
                cryptographicSeed = "2cc2b48c50aefe53b3974ed91e6b4ea9".decodeHex().toByteArray().toByteString(),
                cnPublicKey = "cnPublicKey123"
            )

            coEvery { repository.addTraceLocation(any()) } returns expectedTraceLocation

            val actualTraceLocation = createInstance().createTraceLocation(userInput)

            actualTraceLocation shouldBe expectedTraceLocation

            coVerify(exactly = 1) { repository.addTraceLocation(expectedTraceLocation) }
        }
}
