package de.rki.coronawarnapp.eventregistration.checkins

import com.google.protobuf.ByteString
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.PresenceTracingConfigContainer
import de.rki.coronawarnapp.appconfig.PresenceTracingSubmissionParamContainer
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import de.rki.coronawarnapp.server.protocols.internal.v2
.PresenceTracingParametersOuterClass.PresenceTracingSubmissionParameters.DurationFilter
import de.rki.coronawarnapp.server.protocols.internal.v2
.PresenceTracingParametersOuterClass.PresenceTracingSubmissionParameters.AerosoleDecayFunctionLinear
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.submission.task.TransmissionRiskVectorDeterminator
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDate
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CheckInsTransformerTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var symptoms: Symptoms
    @MockK lateinit var appConfigProvider: AppConfigProvider

    private lateinit var checkInTransformer: CheckInsTransformer

    // CheckIn can not be derived
    private val checkIn1 = CheckIn(
        id = 1L,
        guid = "trace_location_1",
        version = 1,
        type = 1,
        description = "restaurant_1",
        address = "address_1",
        traceLocationStart = null,
        traceLocationEnd = null,
        defaultCheckInLengthInMinutes = null,
        signature = "signature1",
        checkInStart = Instant.parse("2021-03-04T10:21:00Z"),
        checkInEnd = Instant.parse("2021-03-04T10:29:00Z"),
        targetCheckInEnd = null,
        createJournalEntry = false
    )

    /*
      CheckIn that can be derived and can't be splitted
      Derived start and end times
      "expStartDateStr": "2021-03-04 10:20+01:00"
      "expEndDateStr": "2021-03-04 10:40+01:00"
     */
    private val checkIn2 = CheckIn(
        id = 2L,
        guid = "trace_location_2",
        version = 1,
        type = 2,
        description = "restaurant_2",
        address = "address_2",
        traceLocationStart = null,
        traceLocationEnd = null,
        defaultCheckInLengthInMinutes = null,
        signature = "signature_2",
        checkInStart = Instant.parse("2021-03-04T10:20:00Z"),
        checkInEnd = Instant.parse("2021-03-04T10:30:00Z"),
        targetCheckInEnd = null,
        createJournalEntry = false
    )

    // CheckIn that can be derived and can be splitted
    private val checkIn3 = CheckIn(
        id = 3L,
        guid = "trace_location_3",
        version = 1,
        type = 3,
        description = "restaurant_3",
        address = "address_3",
        traceLocationStart = null,
        traceLocationEnd = null,
        defaultCheckInLengthInMinutes = null,
        signature = "signature_3",
        checkInStart = Instant.parse("2021-03-04T09:30:00Z"),
        checkInEnd = Instant.parse("2021-03-06T09:45:00Z"),
        targetCheckInEnd = null,
        createJournalEntry = false
    )

    private val presenceTracingConfig = PresenceTracingSubmissionParamContainer(
        durationFilters = listOf(
            DurationFilter.newBuilder()
                .setDropIfMinutesInRange(
                    RiskCalculationParametersOuterClass.Range.newBuilder()
                        .setMin(0.0)
                        .setMax(10.0)
                        .setMaxExclusive(true)
                        .build()
                )
                .build()
        ),
        aerosoleDecayLinearFunctions = listOf(
            AerosoleDecayFunctionLinear.newBuilder()
                .setMinutesRange(
                    RiskCalculationParametersOuterClass.Range.newBuilder()
                        .setMin(0.0)
                        .setMax(30.0)
                        .build()
                )
                .setSlope(1.0)
                .setIntercept(0.0)
                .build(),
            AerosoleDecayFunctionLinear.newBuilder()
                .setMinutesRange(
                    RiskCalculationParametersOuterClass.Range.newBuilder()
                        .setMin(30.0)
                        .setMax(9999.0)
                        .setMinExclusive(true)
                        .build()
                )
                .setSlope(0.0)
                .setIntercept(30.0)
                .build()
        )
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { timeStamper.nowUTC } returns Instant.now()
        every { symptoms.symptomIndication } returns Symptoms.Indication.POSITIVE
        every { symptoms.startOfSymptoms } returns Symptoms.StartOf.Date(timeStamper.nowUTC.toLocalDate())
        coEvery { appConfigProvider.getAppConfig() } returns mockk<ConfigData>().apply {
            every { presenceTracing } returns PresenceTracingConfigContainer(
                submissionParameters = presenceTracingConfig
            )
        }
        checkInTransformer = CheckInsTransformer(
            timeStamper = timeStamper,
            transmissionDeterminator = TransmissionRiskVectorDeterminator(timeStamper),
            appConfigProvider = appConfigProvider
        )
    }

    @Test
    fun `transform check-ins`() = runBlockingTest {
        val outCheckIns = checkInTransformer.transform(
            listOf(
                checkIn1,
                checkIn2,
                checkIn3
            ),
            symptoms
        )

        with(outCheckIns) {
            size shouldBe 4
            // Check In 1 is excluded from submission due to time deriving

            // Check In 2 mapping and transformation
            get(0).apply {
                /*
                    guid = "trace_location_2",
                    version = 1,
                    type = 2,
                    description = "restaurant_2",
                    address = "address_2",
                    traceLocationStart = null,
                    traceLocationEnd = null,
                    defaultCheckInLengthInMinutes = null,
                    signature = "signature_2",
                 */
                startIntervalNumber shouldBe Instant.parse("2021-03-04T10:20:00Z").seconds // New derived start time
                endIntervalNumber shouldBe Instant.parse("2021-03-04T10:40:00Z").seconds // New derived end time
                signedLocation.signature shouldBe "signature_2".toProtoByteString()
                parseLocation(signedLocation.location).apply {
                    guid shouldBe "trace_location_2"
                    version shouldBe 1
                    type shouldBe TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER
                    description shouldBe "restaurant_2"
                    address shouldBe "address_2"
                    startTimestamp shouldBe 0
                    endTimestamp shouldBe 0
                    defaultCheckInLengthInMinutes shouldBe 0
                }
            }
        }
    }

    private fun parseLocation(bytes: ByteString): TraceLocationOuterClass.TraceLocation =
        TraceLocationOuterClass.TraceLocation.parseFrom(bytes)

    private fun String.toProtoByteString() = ByteString.copyFrom(toByteArray())
}
