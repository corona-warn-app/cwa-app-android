package de.rki.coronawarnapp.presencetracing.checkins

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.PresenceTracingConfigContainer
import de.rki.coronawarnapp.appconfig.PresenceTracingRiskCalculationParamContainer
import de.rki.coronawarnapp.appconfig.PresenceTracingSubmissionParamContainer
import de.rki.coronawarnapp.presencetracing.checkins.cryptography.CheckInCryptography
import de.rki.coronawarnapp.server.protocols.internal.v2.PresenceTracingParametersOuterClass.PresenceTracingSubmissionParameters.AerosoleDecayFunctionLinear
import de.rki.coronawarnapp.server.protocols.internal.v2.PresenceTracingParametersOuterClass.PresenceTracingSubmissionParameters.DurationFilter
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass.Range
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass.TransmissionRiskValueMapping
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.submission.task.TransmissionRiskVectorDeterminer
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.encryption.aes.AesCryptography
import de.rki.coronawarnapp.util.toLocalDateUtc
import de.rki.coronawarnapp.util.toOkioByteString
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.encode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.security.SecureRandom
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlin.random.asKotlinRandom

class CheckInsTransformerTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var symptoms: Symptoms
    @MockK lateinit var appConfigProvider: AppConfigProvider
    private val checkInCryptography = CheckInCryptography(SecureRandom().asKotlinRandom(), AesCryptography())

    private lateinit var checkInTransformer: CheckInsTransformer

    // CheckIn can not be derived
    private val checkIn1 = CheckIn(
        id = 1L,
        traceLocationId = "traceLocationId1".encode(),
        version = 1,
        type = 1,
        description = "restaurant_1",
        address = "address_1",
        traceLocationStart = null,
        traceLocationEnd = null,
        defaultCheckInLengthInMinutes = null,
        cryptographicSeed = "cryptographicSeed".encode(),
        cnPublicKey = "cnPublicKey",
        checkInStart = Instant.parse("2021-03-04T10:21:00Z"),
        checkInEnd = Instant.parse("2021-03-04T10:29:00Z"),
        completed = false,
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
        traceLocationId = "traceLocationId2".encode(),
        version = 1,
        type = 2,
        description = "restaurant_2",
        address = "address_2",
        traceLocationStart = null,
        traceLocationEnd = null,
        defaultCheckInLengthInMinutes = null,
        cryptographicSeed = "cryptographicSeed".encode(),
        cnPublicKey = "cnPublicKey",
        checkInStart = Instant.parse("2021-03-04T10:20:00Z"),
        checkInEnd = Instant.parse("2021-03-04T10:30:00Z"),
        completed = false,
        createJournalEntry = false
    )

    // CheckIn that can be derived and can be splitted
    private val checkIn3 = CheckIn(
        id = 3L,
        traceLocationId = "traceLocationId3".encode(),
        version = 1,
        type = 3,
        description = "restaurant_3",
        address = "address_3",
        traceLocationStart = Instant.parse("2021-03-04T09:00:00Z"),
        traceLocationEnd = Instant.parse("2021-03-10T11:00:00Z"),
        defaultCheckInLengthInMinutes = 10,
        cryptographicSeed = "cryptographicSeed".encode(),
        cnPublicKey = "cnPublicKey",
        checkInStart = Instant.parse("2021-03-04T09:30:00Z"),
        checkInEnd = Instant.parse("2021-03-10T09:45:00Z"),
        completed = false,
        createJournalEntry = false
    )

    private val submissionParams = PresenceTracingSubmissionParamContainer(
        durationFilters = listOf(
            DurationFilter.newBuilder()
                .setDropIfMinutesInRange(
                    Range.newBuilder()
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
                    Range.newBuilder()
                        .setMin(0.0)
                        .setMax(30.0)
                        .build()
                )
                .setSlope(1.0)
                .setIntercept(0.0)
                .build(),
            AerosoleDecayFunctionLinear.newBuilder()
                .setMinutesRange(
                    Range.newBuilder()
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

    private val transmissionRiskValueMappings: List<TransmissionRiskValueMapping> = listOf(
        TransmissionRiskValueMapping.newBuilder()
            .setTransmissionRiskLevel(1)
            .setTransmissionRiskValue(2.0)
            .build(),
        TransmissionRiskValueMapping.newBuilder()
            .setTransmissionRiskLevel(2)
            .setTransmissionRiskValue(2.0)
            .build(),
        TransmissionRiskValueMapping.newBuilder()
            .setTransmissionRiskLevel(4)
            .setTransmissionRiskValue(2.0)
            .build(),
        TransmissionRiskValueMapping.newBuilder()
            .setTransmissionRiskLevel(6)
            .setTransmissionRiskValue(0.0) // CheckIn will be excluded ,as TRV here = 0
            .build(),
        // No transmissionMapping for TRL = 7 and therefore one of the check-ins is excluded
        TransmissionRiskValueMapping.newBuilder()
            .setTransmissionRiskLevel(8)
            .setTransmissionRiskValue(2.0)
            .build()
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { timeStamper.nowUTC } returns Instant.parse("2021-03-11T10:00:00Z")
        every { symptoms.symptomIndication } returns Symptoms.Indication.POSITIVE
        every { symptoms.startOfSymptoms } returns Symptoms.StartOf.Date(timeStamper.nowUTC.toLocalDateUtc())
        coEvery { appConfigProvider.getAppConfig() } returns mockk<ConfigData>().apply {
            every { presenceTracing } returns PresenceTracingConfigContainer(
                submissionParameters = submissionParams,
                riskCalculationParameters = PresenceTracingRiskCalculationParamContainer(
                    transmissionRiskValueMapping = transmissionRiskValueMappings
                )
            )

            every { isUnencryptedCheckInsEnabled } returns true
        }
        checkInTransformer = CheckInsTransformer(
            timeStamper = timeStamper,
            transmissionDeterminator = TransmissionRiskVectorDeterminer(timeStamper),
            checkInCryptography = checkInCryptography,
            appConfigProvider = appConfigProvider
        )
    }

    @Test
    fun `checkInsReport has encryptedCheckIns only`() = runTest {
        coEvery { appConfigProvider.getAppConfig() } returns mockk<ConfigData>().apply {
            every { presenceTracing } returns PresenceTracingConfigContainer(
                submissionParameters = submissionParams,
                riskCalculationParameters = PresenceTracingRiskCalculationParamContainer(
                    transmissionRiskValueMapping = transmissionRiskValueMappings
                )
            )

            every { isUnencryptedCheckInsEnabled } returns false
        }

        val checkInsReport = checkInTransformer.transform(
            listOf(
                checkIn1,
                checkIn2,
                checkIn3
            ),
            symptoms
        )

        checkInsReport.unencryptedCheckIns.size shouldBe 0
        checkInsReport.encryptedCheckIns.size shouldBe 6

        // Note: Testing every item in the list by position is not applicable in this case, because
        // encryptedCheckIns are shuffled
        with(checkInsReport.encryptedCheckIns) {
            filter { it.locationIdHash.toOkioByteString() == checkIn2.traceLocationIdHash }.size shouldBe 1
            filter { it.locationIdHash.toOkioByteString() == checkIn3.traceLocationIdHash }.size shouldBe 5
        }
    }

    @Test
    fun `checkInsReport has unencryptedCheckIns - encryptedCheckIns`() = runTest {
        val checkInsReport = checkInTransformer.transform(
            listOf(
                checkIn1,
                checkIn2,
                checkIn3
            ),
            symptoms
        )

        // 3 check-ins with TRL = 1 and  3 other check-ins with TRL = 2, 4, 8
        checkInsReport.unencryptedCheckIns.size shouldBe 6
        checkInsReport.encryptedCheckIns.size shouldBe 6

        with(checkInsReport.unencryptedCheckIns) {
            // Check In 1 is excluded from submission due to time deriving
            // Check In 2 mapping and transformation
            get(0).apply {
                locationId.toOkioByteString() shouldBe checkIn2.traceLocationId
                // New derived start time
                startIntervalNumber shouldBe Instant.parse("2021-03-04T10:20:00Z").epochSecond / TEN_MINUTES_IN_SECONDS
                // New derived end time
                endIntervalNumber shouldBe Instant.parse("2021-03-04T10:40:00Z").epochSecond / TEN_MINUTES_IN_SECONDS
            }

            // Check-In 3 mappings and transformation
            // Splitted CheckIn 1
            get(1).apply {
                locationId.toOkioByteString() shouldBe checkIn3.traceLocationId
                // Start time from original check-in
                startIntervalNumber shouldBe Instant.parse("2021-03-04T09:30:00Z").epochSecond / TEN_MINUTES_IN_SECONDS
                // End time for splitted check-in 1
                endIntervalNumber shouldBe Instant.parse("2021-03-05T00:00:00Z").epochSecond / TEN_MINUTES_IN_SECONDS
            }

            // Splitted CheckIn 2
            get(2).apply {
                locationId.toOkioByteString() shouldBe checkIn3.traceLocationId

                // Start time for splitted check-in 2
                startIntervalNumber shouldBe Instant.parse("2021-03-05T00:00:00Z").epochSecond / TEN_MINUTES_IN_SECONDS
                // End time for splitted check-in 2
                endIntervalNumber shouldBe Instant.parse("2021-03-06T00:00:00Z").epochSecond / TEN_MINUTES_IN_SECONDS
            }

            // Splitted CheckIn 3
            get(3).apply {
                locationId.toOkioByteString() shouldBe checkIn3.traceLocationId
                // Start time from splitted check-in 3
                startIntervalNumber shouldBe Instant.parse("2021-03-06T00:00:00Z").epochSecond / TEN_MINUTES_IN_SECONDS
                // End time for splitted check-in 3
                endIntervalNumber shouldBe Instant.parse("2021-03-07T00:00:00Z").epochSecond / TEN_MINUTES_IN_SECONDS
            }

            // Splitted CheckIn 4
            get(4).apply {
                locationId.toOkioByteString() shouldBe checkIn3.traceLocationId
                // Start time from splitted check-in 4
                startIntervalNumber shouldBe Instant.parse("2021-03-07T00:00:00Z").epochSecond / TEN_MINUTES_IN_SECONDS
                // End time for splitted check-in 4
                endIntervalNumber shouldBe Instant.parse("2021-03-08T00:00:00Z").epochSecond / TEN_MINUTES_IN_SECONDS
            }

            // Splitted CheckIn 5
            get(5).apply {
                locationId.toOkioByteString() shouldBe checkIn3.traceLocationId
                // Start time from splitted check-in 5
                startIntervalNumber shouldBe Instant.parse("2021-03-10T00:00:00Z").epochSecond / TEN_MINUTES_IN_SECONDS
                // End time for splitted check-in 5
                endIntervalNumber shouldBe Instant.parse("2021-03-10T10:20:00Z").epochSecond / TEN_MINUTES_IN_SECONDS
            }
        }

        // Note: Testing every item in the list by position is not applicable in this case, because
        // encryptedCheckIns are shuffled
        with(checkInsReport.encryptedCheckIns) {
            filter { it.locationIdHash.toOkioByteString() == checkIn2.traceLocationIdHash }.size shouldBe 1
            filter { it.locationIdHash.toOkioByteString() == checkIn3.traceLocationIdHash }.size shouldBe 5
        }
    }

    companion object {
        private val TEN_MINUTES_IN_SECONDS = TimeUnit.MINUTES.toSeconds(10)
    }
}
