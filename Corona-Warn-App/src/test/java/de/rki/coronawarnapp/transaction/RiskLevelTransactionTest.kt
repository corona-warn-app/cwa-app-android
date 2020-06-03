package de.rki.coronawarnapp.transaction

import com.google.android.gms.nearby.exposurenotification.ExposureSummary
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.risk.RiskLevel
import de.rki.coronawarnapp.risk.RiskLevel.INCREASED_RISK
import de.rki.coronawarnapp.risk.RiskLevel.LOW_LEVEL_RISK
import de.rki.coronawarnapp.risk.RiskLevel.NO_CALCULATION_POSSIBLE_TRACING_OFF
import de.rki.coronawarnapp.risk.RiskLevel.UNDETERMINED
import de.rki.coronawarnapp.risk.RiskLevel.UNKNOWN_RISK_INITIAL
import de.rki.coronawarnapp.risk.RiskLevel.UNKNOWN_RISK_OUTDATED_RESULTS
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass.RiskScoreClass
import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass.RiskScoreClassification
import de.rki.coronawarnapp.service.riskscoreclassification.RiskScoreClassificationService
import de.rki.coronawarnapp.storage.ExposureSummaryRepository
import de.rki.coronawarnapp.storage.RiskLevelRepository
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class RiskLevelTransactionTest {

    @MockK
    private lateinit var esRepositoryMock: ExposureSummaryRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkObject(InternalExposureNotificationClient)
        mockkObject(RiskScoreClassificationService)
        mockkObject(RiskLevelRepository)
        mockkObject(RiskLevelTransaction)
        mockkObject(TimeVariables)
        mockkObject(ExposureSummaryRepository.Companion)
        mockkObject(RiskLevel.Companion)

        every { ExposureSummaryRepository.getExposureSummaryRepository() } returns esRepositoryMock

        every { RiskLevelRepository.getLastCalculatedScore() } returns UNDETERMINED

        every { RiskLevelRepository.setRiskLevelScore(any()) } just Runs
        every { RiskLevel.riskLevelChangedBetweenLowAndHigh(any(), any()) } returns false
    }

    /** Test case for [NO_CALCULATION_POSSIBLE_TRACING_OFF] */
    @Test
    fun noCalculationPossibleTracingOff() {

        val testRiskLevel = NO_CALCULATION_POSSIBLE_TRACING_OFF

        // tracing is deactivated
        coEvery { InternalExposureNotificationClient.asyncIsEnabled() } returns false

        runBlocking {

            RiskLevelTransaction.start()

            coVerifyOrder {
                RiskLevelTransaction.start()

                RiskLevelTransaction["executeCheckTracing"]()
                RiskLevelTransaction["isValidResult"](testRiskLevel)

                RiskLevelRepository.setRiskLevelScore(testRiskLevel)
                RiskLevelTransaction["executeClose"]()
            }
        }
    }

    /** Test case for [UNKNOWN_RISK_INITIAL] if no keys are fetched from server */
    @Test
    fun unknownRiskInitialNoKeys() {

        val testRiskLevel = UNKNOWN_RISK_INITIAL

        // tracing is activated
        coEvery { InternalExposureNotificationClient.asyncIsEnabled() } returns true

        // we have not fetched keys from server yet
        every { TimeVariables.getLastTimeDiagnosisKeysFromServerFetch() } returns null

        runBlocking {

            RiskLevelTransaction.start()

            coVerifyOrder {
                RiskLevelTransaction.start()

                RiskLevelTransaction["executeCheckTracing"]()
                RiskLevelTransaction["isValidResult"](UNDETERMINED)

                RiskLevelTransaction["executeCheckUnknownRiskInitialNoKeys"]()
                RiskLevelTransaction["isValidResult"](testRiskLevel)

                RiskLevelRepository.setRiskLevelScore(testRiskLevel)
                RiskLevelTransaction["executeClose"]()
            }
        }
    }

    /** Test case for [UNKNOWN_RISK_OUTDATED_RESULTS] if keys are outdated */
    @Test
    fun unknownRiskOutdatedResults() {

        val testRiskLevel = UNKNOWN_RISK_OUTDATED_RESULTS

        val twoDaysAboveMaxStale = TimeUnit.DAYS.toMillis(TimeVariables.getMaxStaleExposureRiskRange().plus(2).toLong())

        // tracing is activated
        coEvery { InternalExposureNotificationClient.asyncIsEnabled() } returns true

        // the last time we fetched keys from the server is above the threshold
        every { TimeVariables.getLastTimeDiagnosisKeysFromServerFetch() } returns System.currentTimeMillis()
            .minus(twoDaysAboveMaxStale)

        // active tracing time is 1h above the threshold
        every { TimeVariables.getTimeActiveTracingDuration() } returns TimeUnit.HOURS.toMillis(
            TimeVariables.getMinActivatedTracingTime().plus(1).toLong()
        )

        runBlocking {

            RiskLevelTransaction.start()

            coVerifyOrder {
                RiskLevelTransaction.start()

                RiskLevelTransaction["executeCheckTracing"]()
                RiskLevelTransaction["isValidResult"](UNDETERMINED)

                RiskLevelTransaction["executeCheckUnknownRiskInitialNoKeys"]()
                RiskLevelTransaction["isValidResult"](UNDETERMINED)

                RiskLevelTransaction["executeCheckUnknownRiskOutdatedResults"]()
                RiskLevelTransaction["isValidResult"](testRiskLevel)

                RiskLevelRepository.setRiskLevelScore(testRiskLevel)
                RiskLevelTransaction["executeClose"]()
            }
        }
    }

    /** Test case for [INCREASED_RISK]  */
    @Test
    fun increasedRisk() {

        val testRiskLevel = INCREASED_RISK

        val testRiskScoreClassification = buildRiskScoreClassification(
            2749,
            2750,
            4096
        )

        val testExposureSummary = buildSummary(2751)

        // tracing is activated
        coEvery { InternalExposureNotificationClient.asyncIsEnabled() } returns true

        // the last time we fetched keys from the server happened 30 mins ago (within maxStale)
        every { TimeVariables.getLastTimeDiagnosisKeysFromServerFetch() } returns System.currentTimeMillis()
            .minus(TimeUnit.MINUTES.toMillis(30))

        // We traced only 2h
        every { TimeVariables.getTimeActiveTracingDuration() } returns TimeUnit.HOURS.toMillis(2)

        // the risk score of the last exposure summary is above the high min threshold
        coEvery { RiskScoreClassificationService.asyncRetrieveRiskScoreClassification() } returns testRiskScoreClassification
        coEvery { esRepositoryMock.getLatestExposureSummary() } returns testExposureSummary

        runBlocking {

            RiskLevelTransaction.start()

            coVerifyOrder {
                RiskLevelTransaction.start()

                RiskLevelTransaction["executeCheckTracing"]()
                RiskLevelTransaction["isValidResult"](UNDETERMINED)

                RiskLevelTransaction["executeCheckUnknownRiskInitialNoKeys"]()
                RiskLevelTransaction["isValidResult"](UNDETERMINED)

                RiskLevelTransaction["executeCheckUnknownRiskOutdatedResults"]()
                RiskLevelTransaction["isValidResult"](UNDETERMINED)

                RiskLevelTransaction["executeRetrieveRiskThreshold"]()

                RiskLevelTransaction["executeRetrieveExposureSummary"]()

                RiskLevelTransaction["executeCheckIncreasedRisk"](testRiskScoreClassification, testExposureSummary)
                RiskLevelTransaction["isValidResult"](testRiskLevel)

                RiskLevelRepository.setRiskLevelScore(testRiskLevel)
                RiskLevelTransaction["executeClose"]()
            }
        }
    }

    /** Test case for [UNKNOWN_RISK_INITIAL] if tracing threshold is not reached */
    @Test
    fun unknownRiskInitialTracingDuration() {

        val testRiskLevel = UNKNOWN_RISK_INITIAL

        val testRiskScoreClassification = buildRiskScoreClassification(
            2749,
            2750,
            4096
        )

        val testExposureSummary = buildSummary(2749)

        val twoHoursBelowMinActiveTracingDuration =
            TimeUnit.HOURS.toMillis(TimeVariables.getMinActivatedTracingTime().minus(2).toLong())

        // tracing is activated
        coEvery { InternalExposureNotificationClient.asyncIsEnabled() } returns true

        // the last time we fetched keys from the server happened 30 mins ago (within maxStale)
        every { TimeVariables.getLastTimeDiagnosisKeysFromServerFetch() } returns System.currentTimeMillis()
            .minus(TimeUnit.MINUTES.toMillis(30))

        // we only traced 2 hours
        every { TimeVariables.getTimeActiveTracingDuration() } returns twoHoursBelowMinActiveTracingDuration

        // the exposure summary risk score is not below high min score
        coEvery { RiskScoreClassificationService.asyncRetrieveRiskScoreClassification() } returns testRiskScoreClassification
        coEvery { esRepositoryMock.getLatestExposureSummary() } returns testExposureSummary

        runBlocking {

            RiskLevelTransaction.start()

            coVerifyOrder {
                RiskLevelTransaction.start()

                RiskLevelTransaction["executeCheckTracing"]()
                RiskLevelTransaction["isValidResult"](UNDETERMINED)

                RiskLevelTransaction["executeCheckUnknownRiskInitialNoKeys"]()
                RiskLevelTransaction["isValidResult"](UNDETERMINED)

                RiskLevelTransaction["executeCheckUnknownRiskOutdatedResults"]()
                RiskLevelTransaction["isValidResult"](UNDETERMINED)

                RiskLevelTransaction["executeRetrieveRiskThreshold"]()

                RiskLevelTransaction["executeRetrieveExposureSummary"]()

                RiskLevelTransaction["executeCheckIncreasedRisk"](testRiskScoreClassification, testExposureSummary)
                RiskLevelTransaction["isValidResult"](UNDETERMINED)

                RiskLevelTransaction["executeCheckUnknownRiskInitialTracingDuration"]()
                RiskLevelTransaction["isValidResult"](testRiskLevel)

                RiskLevelRepository.setRiskLevelScore(testRiskLevel)
                RiskLevelTransaction["executeClose"]()
            }
        }
    }

    /** Test case for [LOW_LEVEL_RISK] */
    @Test
    fun lowRisk() {

        val testRiskLevel = LOW_LEVEL_RISK

        val testRiskScoreClassification = buildRiskScoreClassification(
            2749,
            2750,
            4096
        )

        val testExposureSummary = buildSummary(2749)

        val twoHoursAboveMinActiveTracingDuration =
            TimeUnit.HOURS.toMillis(TimeVariables.getMinActivatedTracingTime().plus(2).toLong())

        // tracing is activated
        coEvery { InternalExposureNotificationClient.asyncIsEnabled() } returns true

        // the last time we fetched keys from the server happened 30 mins ago (within maxStale)
        every { TimeVariables.getLastTimeDiagnosisKeysFromServerFetch() } returns System.currentTimeMillis()
            .minus(TimeUnit.MINUTES.toMillis(30))

        // the active tracing duration is above the threshold
        every { TimeVariables.getTimeActiveTracingDuration() } returns twoHoursAboveMinActiveTracingDuration

        coEvery { RiskScoreClassificationService.asyncRetrieveRiskScoreClassification() } returns testRiskScoreClassification
        coEvery { esRepositoryMock.getLatestExposureSummary() } returns testExposureSummary

        runBlocking {

            RiskLevelTransaction.start()

            coVerifyOrder {
                RiskLevelTransaction.start()

                RiskLevelTransaction["executeCheckTracing"]()
                RiskLevelTransaction["isValidResult"](UNDETERMINED)

                RiskLevelTransaction["executeCheckUnknownRiskInitialNoKeys"]()
                RiskLevelTransaction["isValidResult"](UNDETERMINED)

                RiskLevelTransaction["executeCheckUnknownRiskOutdatedResults"]()
                RiskLevelTransaction["isValidResult"](UNDETERMINED)

                RiskLevelTransaction["executeRetrieveRiskThreshold"]()

                RiskLevelTransaction["executeRetrieveExposureSummary"]()

                RiskLevelTransaction["executeCheckIncreasedRisk"](testRiskScoreClassification, testExposureSummary)
                RiskLevelTransaction["isValidResult"](UNDETERMINED)

                RiskLevelTransaction["executeCheckUnknownRiskInitialTracingDuration"]()
                RiskLevelTransaction["isValidResult"](UNDETERMINED)

                RiskLevelRepository.setRiskLevelScore(testRiskLevel)
                RiskLevelTransaction["executeClose"]()
            }
        }
    }

    @After
    fun cleanUp() {
        unmockkAll()
    }

    private fun buildRiskScoreClassification(
        lowMax: Int,
        highMin: Int,
        highMax: Int
    ): RiskScoreClassification {
        val mockUrl = "https://corona-warn.app"
        val lowLabel = "LOW"
        val highLabel = "HIGH"

        val lowClass = RiskScoreClass.newBuilder()
            .setLabel(lowLabel)
            .setMax(lowMax)
            .setUrl(mockUrl)
            .build()

        val highClass = RiskScoreClass.newBuilder()
            .setLabel(highLabel)
            .setMin(highMin)
            .setMax(highMax)
            .setUrl(mockUrl)
            .build()
        val riskScoreClasses = mutableListOf(lowClass, highClass)

        return RiskScoreClassification
            .newBuilder()
            .addAllRiskClasses(riskScoreClasses)
            .build()
    }

    private fun buildSummary(maxRisk: Int): ExposureSummary {
        return ExposureSummary.ExposureSummaryBuilder().setMaximumRiskScore(maxRisk).build()
    }
}
