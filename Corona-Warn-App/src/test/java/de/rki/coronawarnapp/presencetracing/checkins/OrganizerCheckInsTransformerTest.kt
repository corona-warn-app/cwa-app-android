package de.rki.coronawarnapp.presencetracing.checkins

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.PresenceTracingConfigContainer
import de.rki.coronawarnapp.appconfig.PresenceTracingRiskCalculationParamContainer
import de.rki.coronawarnapp.appconfig.PresenceTracingSubmissionParamContainer
import de.rki.coronawarnapp.presencetracing.checkins.cryptography.CheckInCryptography
import de.rki.coronawarnapp.server.protocols.internal.v2.PresenceTracingParametersOuterClass
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass
import de.rki.coronawarnapp.util.encryption.aes.AesCryptography
import de.rki.coronawarnapp.util.toOkioByteString
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.encode
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.security.SecureRandom
import kotlin.random.asKotlinRandom

@Disabled
internal class OrganizerCheckInsTransformerTest : BaseTest() {

    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var configData: ConfigData
    private val checkInCryptography = CheckInCryptography(SecureRandom().asKotlinRandom(), AesCryptography())
    private lateinit var checkInTransformer: OrganizerCheckInsTransformer

    private val submissionParams = PresenceTracingSubmissionParamContainer(
        durationFilters = listOf(
            PresenceTracingParametersOuterClass.PresenceTracingSubmissionParameters.DurationFilter.newBuilder()
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
            PresenceTracingParametersOuterClass.PresenceTracingSubmissionParameters.AerosoleDecayFunctionLinear
                .newBuilder()
                .setMinutesRange(
                    RiskCalculationParametersOuterClass.Range.newBuilder()
                        .setMin(0.0)
                        .setMax(30.0)
                        .build()
                )
                .setSlope(1.0)
                .setIntercept(0.0)
                .build(),
            PresenceTracingParametersOuterClass.PresenceTracingSubmissionParameters.AerosoleDecayFunctionLinear
                .newBuilder()
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

    private val transmissionRiskValueMappings: List<RiskCalculationParametersOuterClass.TransmissionRiskValueMapping> =
        listOf(
            RiskCalculationParametersOuterClass.TransmissionRiskValueMapping.newBuilder()
                .setTransmissionRiskLevel(5)
                .setTransmissionRiskValue(2.0)
                .build()
        )

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

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { appConfigProvider.getAppConfig() } returns configData
        every { configData.presenceTracing } returns PresenceTracingConfigContainer(
            submissionParameters = submissionParams,
            riskCalculationParameters = PresenceTracingRiskCalculationParamContainer(
                transmissionRiskValueMapping = transmissionRiskValueMappings
            )
        )

        every { configData.isUnencryptedCheckInsEnabled } returns true

        checkInTransformer = OrganizerCheckInsTransformer(
            checkInCryptography = checkInCryptography,
            appConfigProvider = appConfigProvider
        )
    }

    @Test
    fun `CheckIn1 can not derived - Encrypted and Unencrypted CheckIns should be empty`() = runTest {
        checkInTransformer.transform(listOf(checkIn1)).apply {
            unencryptedCheckIns.size shouldBe 0
            encryptedCheckIns.size shouldBe 0
        }
    }

    @Test
    fun `CheckIn1 can not derived - Encrypted CheckIns should be empty`() = runTest {
        every { configData.isUnencryptedCheckInsEnabled } returns false
        checkInTransformer.transform(listOf(checkIn1)).apply {
            unencryptedCheckIns.size shouldBe 0
            encryptedCheckIns.size shouldBe 0
        }
    }

    @Test
    fun `CheckIn2 can not split - Encrypted and Unencrypted CheckIns should be 1`() = runTest {
        checkInTransformer.transform(listOf(checkIn2)).apply {
            unencryptedCheckIns.size shouldBe 1
            encryptedCheckIns.size shouldBe 1
        }
    }

    @Test
    fun `CheckIn2 can not split - Encrypted CheckIns should be 1`() = runTest {
        every { configData.isUnencryptedCheckInsEnabled } returns false
        checkInTransformer.transform(listOf(checkIn2)).apply {
            unencryptedCheckIns.size shouldBe 0
            encryptedCheckIns.size shouldBe 1
        }
    }

    @Test
    fun `CheckIn3 can be split - Encrypted and Unencrypted CheckIns should be 7`() = runTest {
        checkInTransformer.transform(listOf(checkIn3)).apply {
            unencryptedCheckIns.size shouldBe 7
            encryptedCheckIns.size shouldBe 7

            unencryptedCheckIns.forEach {
                it.transmissionRiskLevel shouldBe 5
                it.locationId.toOkioByteString() shouldBe checkIn3.traceLocationId
            }

            encryptedCheckIns.forEach {
                it.locationIdHash.toOkioByteString() shouldBe checkIn3.traceLocationIdHash
            }
        }
    }

    @Test
    fun `CheckIn3 can be split - Encrypted CheckIns should be 7`() = runTest {
        every { configData.isUnencryptedCheckInsEnabled } returns false
        checkInTransformer.transform(listOf(checkIn3)).apply {
            unencryptedCheckIns.size shouldBe 0
            encryptedCheckIns.size shouldBe 7

            encryptedCheckIns.forEach {
                it.locationIdHash.toOkioByteString() shouldBe checkIn3.traceLocationIdHash
            }
        }
    }
}
