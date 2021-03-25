package de.rki.coronawarnapp.eventregistration.events.server

import dagger.Lazy
import de.rki.coronawarnapp.eventregistration.events.TraceLocationUserInput
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import retrofit2.Response
import testhelpers.BaseTest

internal class TraceLocationServerTest : BaseTest() {

    @MockK lateinit var api: Lazy<CreateTraceLocationApiV1>

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun tearDown() {
    }

    private fun getInstance() = TraceLocationServer(api)

    @Test
    fun `retrieveSignedTraceLocation() should return SignedTraceLocation when everything works fine`() =
        runBlockingTest {

            val signedTraceLocationMock = TraceLocationData.signedTraceLocationTemporary
            coEvery { api.get().createTraceLocation(any()) } returns Response.success(200, signedTraceLocationMock)

            val userInput = TraceLocationData.traceLocationTemporaryUserInput
            val actualSignedTraceLocation = getInstance().retrieveSignedTraceLocation(userInput)

            actualSignedTraceLocation shouldBe signedTraceLocationMock
        }

    @Test
    fun `retrieveSignedTraceLocation() should throw HttpException when we receive an unsuccessful response`() =
        runBlockingTest {
            coEvery { api.get().createTraceLocation(any()) } returns Response.error(
                400,
                "Client Error".toResponseBody()
            )

            shouldThrow<HttpException> {
                getInstance().retrieveSignedTraceLocation(TraceLocationData.traceLocationTemporaryUserInput)
            }
        }

    // TODO: Add additional error handling tests once it is defined in TecSpec

    @Test
    fun `toTraceLocationProtoBuf() should map user input correctly for temporary trace locations`() {
        TraceLocationUserInput(
            type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER,
            description = "Event Registration Release Party",
            address = "SAP Headquarter",
            startDate = Instant.parse("2021-05-01T19:00:00.000Z"),
            endDate = Instant.parse("2021-05-01T23:30:00.000Z"),
            defaultCheckInLengthInMinutes = 180
        ).toTraceLocationProtoBuf().run {
            guid shouldBe ""
            version shouldBe 1
            type shouldBe TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER
            description shouldBe "Event Registration Release Party"
            address shouldBe "SAP Headquarter"
            startTimestamp shouldBe Instant.parse("2021-05-01T19:00:00.000Z").seconds
            endTimestamp shouldBe Instant.parse("2021-05-01T23:30:00.000Z").seconds
            defaultCheckInLengthInMinutes shouldBe 180
        }
    }

    @Test
    fun `toTraceLocationProtoBuf() should map user input correctly for permanent trace locations`() {
        TraceLocationUserInput(
            type = TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_OTHER,
            description = "IceCream Shop",
            address = "IceCream Wonderland Street 1",
            startDate = null,
            endDate = null,
            defaultCheckInLengthInMinutes = 30
        ).toTraceLocationProtoBuf().run {
            guid shouldBe ""
            version shouldBe 1
            type shouldBe TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_OTHER
            description shouldBe "IceCream Shop"
            address shouldBe "IceCream Wonderland Street 1"
            startTimestamp shouldBe 0
            endTimestamp shouldBe 0
            defaultCheckInLengthInMinutes shouldBe 30
        }
    }
}
