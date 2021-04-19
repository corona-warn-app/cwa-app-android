package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
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
        traceLocationDescription: String,
        traceLocationAddress: String,
    ) = mockk<TraceLocation>().apply {
        every { id } returns traceLocationId
        every { description } returns traceLocationDescription
        every { address } returns traceLocationAddress
    }

    @Test
    fun `checkLog() should return LogLine with censored trace location information`() = runBlocking {
        every { traceLocationRepo.allTraceLocations } returns flowOf(
            listOf(
                mockTraceLocation(
                    traceLocationId = 1,
                    traceLocationDescription = "Sushi Place",
                    traceLocationAddress = "Sushi Street 123, 12345 Fish Town"
                ),
                mockTraceLocation(
                    traceLocationId = 2,
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
                Yesterday we went to the Rick Astley Concert. The spectacle took place in Never gonna give you up street 1, 12345 RickRoll City. 
                Afterwards we had some food in Sushi Place in Sushi Street 123, 12345 Fish Town.    
                """.trimIndent(),
            tag = "I am tag",
            throwable = null
        )

        censor.checkLog(logLineToCensor) shouldBe logLineToCensor.copy(
            message =
                """
                Yesterday we went to the TraceLocation#2/Description. The spectacle took place in TraceLocation#2/Address. 
                Afterwards we had some food in TraceLocation#1/Description in TraceLocation#1/Address.    
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
                    traceLocationDescription = "Description 1",
                    traceLocationAddress = "Address 1"
                ),
                mockTraceLocation(
                    traceLocationId = 2,
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
