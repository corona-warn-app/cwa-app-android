package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.censors.presencetracing.TraceLocationCensor
import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class TraceLocationCensorTest : BaseTest() {

    @MockK lateinit var traceLocationRepo: TraceLocationRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    private fun createInstance(scope: CoroutineScope) = TraceLocationCensor(
        debugScope = scope,
        traceLocationRepository = traceLocationRepo
    )

    private fun mockTraceLocation(
        traceLocationId: Long,
        traceLocationType: TraceLocationOuterClass.TraceLocationType,
        traceLocationDescription: String,
        traceLocationAddress: String,
    ) = mockk<TraceLocation>().apply {
        every { id } returns traceLocationId
        every { type } returns traceLocationType
        every { description } returns traceLocationDescription
        every { address } returns traceLocationAddress
    }

    @Test
    fun `checkLog() should return LogLine with censored trace location information`() = runBlocking {
        every { traceLocationRepo.allTraceLocations } returns flowOf(
            listOf(
                mockTraceLocation(
                    traceLocationId = 1,
                    traceLocationType = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_FOOD_SERVICE,
                    traceLocationDescription = "Sushi Place",
                    traceLocationAddress = "Sushi Street 123, 12345 Fish Town"
                ),
                mockTraceLocation(
                    traceLocationId = 2,
                    traceLocationType = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_CULTURAL_EVENT,
                    traceLocationDescription = "Rick Astley Concert",
                    traceLocationAddress = "Never gonna give you up street 1, 12345 RickRoll City"
                )
            )
        )

        val censor = createInstance(this)

        val logLineToCensor = LogLine(
            timestamp = 1,
            priority = 3,
            message =
                """
                The type is LOCATION_TYPE_TEMPORARY_CULTURAL_EVENT. Yesterday we went to the Rick Astley Concert. The spectacle took place in Never gonna give you up street 1, 12345 RickRoll City. 
                Afterwards we had some food in Sushi Place in Sushi Street 123, 12345 Fish Town. It a nice LOCATION_TYPE_PERMANENT_FOOD_SERVICE.
                """.trimIndent(),
            tag = "I am tag",
            throwable = null
        )

        censor.checkLog(logLineToCensor) shouldBe logLineToCensor.copy(
            message =
                """
                The type is TraceLocation#2/Type. Yesterday we went to the TraceLocation#2/Description. The spectacle took place in TraceLocation#2/Address. 
                Afterwards we had some food in TraceLocation#1/Description in TraceLocation#1/Address. It a nice TraceLocation#1/Type.
                """.trimIndent()
        )

        // censoring should still work after the user deletes his trace locations
        every { traceLocationRepo.allTraceLocations } returns flowOf(emptyList())

        censor.checkLog(logLineToCensor) shouldBe logLineToCensor.copy(
            message =
                """
                The type is TraceLocation#2/Type. Yesterday we went to the TraceLocation#2/Description. The spectacle took place in TraceLocation#2/Address. 
                Afterwards we had some food in TraceLocation#1/Description in TraceLocation#1/Address. It a nice TraceLocation#1/Type.
                """.trimIndent()
        )
    }

    @Test
    fun `checkLog() should return null if no trace locations are stored`() = runBlockingTest {
        every { traceLocationRepo.allTraceLocations } returns flowOf(emptyList())

        val censor = createInstance(this)
        val logLine = LogLine(
            timestamp = 1,
            priority = 3,
            message = "Lorem ipsum",
            tag = "I'm a tag",
            throwable = null
        )
        censor.checkLog(logLine) shouldBe null
    }

    @Test
    fun `checkLog() should return null if LogLine doesn't need to be censored`() = runBlockingTest {

        every { traceLocationRepo.allTraceLocations } returns flowOf(
            listOf(
                mockTraceLocation(
                    traceLocationId = 1,
                    traceLocationType = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_CULTURAL_EVENT,
                    traceLocationDescription = "Description 1",
                    traceLocationAddress = "Address 1"
                ),
                mockTraceLocation(
                    traceLocationId = 2,
                    traceLocationType = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_CULTURAL_EVENT,
                    traceLocationDescription = "Description 2",
                    traceLocationAddress = "Address 2"
                )
            )
        )

        val censor = createInstance(this)
        val logLine = LogLine(
            timestamp = 1,
            priority = 3,
            message = "Lorem ipsum",
            tag = "I'm a tag",
            throwable = null
        )

        censor.checkLog(logLine) shouldBe null
    }
}
