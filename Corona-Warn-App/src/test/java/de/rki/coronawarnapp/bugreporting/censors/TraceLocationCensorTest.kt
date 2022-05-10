package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.censors.presencetracing.TraceLocationCensor
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.presencetracing.locations.TraceLocationUserInput
import de.rki.coronawarnapp.presencetracing.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

@Suppress("MaxLineLength")
internal class TraceLocationCensorTest : BaseTest() {

    @MockK lateinit var traceLocationRepo: TraceLocationRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        TraceLocationCensor.dataToCensor = null
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
    fun `checkLog() should return LogLine with censored trace location information from repository`() =
        runTest(UnconfinedTestDispatcher()) {
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

            val logLineToCensor =
                """
                The type is LOCATION_TYPE_TEMPORARY_CULTURAL_EVENT. Yesterday we went to the Rick Astley Concert. The spectacle took place in Never gonna give you up street 1, 12345 RickRoll City. 
                Afterwards we had some food in Sushi Place in Sushi Street 123, 12345 Fish Town. It a nice LOCATION_TYPE_PERMANENT_FOOD_SERVICE.
                """.trimIndent()

            censor.checkLog(logLineToCensor)!!.compile()!!.censored shouldBe
                """
                The type is TraceLocation#2/Type. Yesterday we went to the TraceLocation#2/Description. The spectacle took place in TraceLocation#2/Address. 
                Afterwards we had some food in TraceLocation#1/Description in TraceLocation#1/Address. It a nice TraceLocation#1/Type.
                """.trimIndent()
        }

    @Test
    fun `censoring should still work after the user deletes his trace locations`() = runTest(UnconfinedTestDispatcher()) {

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
            ),
            listOf(
                mockTraceLocation(
                    traceLocationId = 1,
                    traceLocationType = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_FOOD_SERVICE,
                    traceLocationDescription = "Sushi Place",
                    traceLocationAddress = "Sushi Street 123, 12345 Fish Town"
                ),
            )
        )

        val censor = createInstance(this)

        val logLineToCensor =
            """
            The type is LOCATION_TYPE_TEMPORARY_CULTURAL_EVENT. Yesterday we went to the Rick Astley Concert. The spectacle took place in Never gonna give you up street 1, 12345 RickRoll City. 
            Afterwards we had some food in Sushi Place in Sushi Street 123, 12345 Fish Town. It a nice LOCATION_TYPE_PERMANENT_FOOD_SERVICE.
            """.trimIndent()

        censor.checkLog(logLineToCensor)!!.compile()!!.censored shouldBe
            """
            The type is TraceLocation#2/Type. Yesterday we went to the TraceLocation#2/Description. The spectacle took place in TraceLocation#2/Address. 
            Afterwards we had some food in TraceLocation#1/Description in TraceLocation#1/Address. It a nice TraceLocation#1/Type.
            """.trimIndent()
    }

    @Test
    fun `checkLog() should return LogLine with censored trace location information from companion object`() =
        runTest(UnconfinedTestDispatcher()) {
            every { traceLocationRepo.allTraceLocations } returns flowOf(emptyList())
            TraceLocationCensor.dataToCensor = TraceLocationUserInput(
                type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_PRIVATE_EVENT,
                description = "Top Secret Private Event",
                address = "top secret address",
                startDate = null,
                endDate = null,
                defaultCheckInLengthInMinutes = 180
            )

            val censor = createInstance(this)

            val logLineToCensor =
                """
                The user just created a new traceLocation with Top Secret Private Event as the description and
                top secret address as the address. The type is LOCATION_TYPE_TEMPORARY_PRIVATE_EVENT. 
                """.trimIndent()

            censor.checkLog(logLineToCensor)!!.compile()!!.censored shouldBe
                """
                The user just created a new traceLocation with TraceLocationUserInput#Description as the description and
                TraceLocationUserInput#Address as the address. The type is TraceLocationUserInput#Type. 
                """.trimIndent()
        }

    @Test
    fun `checkLog() should return null if no trace locations are stored`() = runTest(UnconfinedTestDispatcher()) {
        every { traceLocationRepo.allTraceLocations } returns flowOf(emptyList())

        val censor = createInstance(this)
        val logLine = "Lorem ipsum"
        censor.checkLog(logLine) shouldBe null
    }

    @Test
    fun `checkLog() should return null if LogLine doesn't need to be censored`() = runTest(UnconfinedTestDispatcher()) {

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
        val logLine = "Lorem ipsum"

        censor.checkLog(logLine) shouldBe null
    }
}
