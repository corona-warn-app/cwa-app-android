package de.rki.coronawarnapp.transaction

import android.content.Context
import com.google.android.gms.nearby.exposurenotification.ExposureSummary
import de.rki.coronawarnapp.CoronaWarnApplication
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.risk.RiskLevel
import de.rki.coronawarnapp.risk.RiskLevel.INCREASED_RISK
import de.rki.coronawarnapp.risk.RiskLevel.LOW_LEVEL_RISK
import de.rki.coronawarnapp.risk.RiskLevel.NO_CALCULATION_POSSIBLE_TRACING_OFF
import de.rki.coronawarnapp.risk.RiskLevel.UNDETERMINED
import de.rki.coronawarnapp.risk.RiskLevel.UNKNOWN_RISK_INITIAL
import de.rki.coronawarnapp.risk.RiskLevel.UNKNOWN_RISK_OUTDATED_RESULTS
import de.rki.coronawarnapp.risk.RiskLevel.UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass
import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass.RiskScoreClass
import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass.RiskScoreClassification
import de.rki.coronawarnapp.service.applicationconfiguration.ApplicationConfigurationService
import de.rki.coronawarnapp.storage.ExposureSummaryRepository
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.RiskLevelRepository
import de.rki.coronawarnapp.util.ConnectivityHelper
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
import java.util.UUID
import java.util.concurrent.TimeUnit

class RiskLevelTransactionTest {

    @MockK
    private lateinit var esRepositoryMock: ExposureSummaryRepository

    @MockK
    private lateinit var context: Context

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkObject(InternalExposureNotificationClient)
        mockkObject(ApplicationConfigurationService)
        mockkObject(LocalData)
        every { LocalData.lastSuccessfullyCalculatedRiskLevel() } returns UNDETERMINED
        mockkObject(RiskLevelRepository)
        mockkObject(RiskLevelTransaction)
        mockkObject(TimeVariables)
        mockkObject(ExposureSummaryRepository.Companion)
        mockkObject(RiskLevel.Companion)
        mockkObject(ConnectivityHelper)
        mockkObject(CoronaWarnApplication)

        every { ExposureSummaryRepository.getExposureSummaryRepository() } returns esRepositoryMock

        every { RiskLevelRepository.getLastCalculatedScore() } returns UNDETERMINED

        every { RiskLevelRepository.setRiskLevelScore(any()) } just Runs
        every { RiskLevel.riskLevelChangedBetweenLowAndHigh(any(), any()) } returns false
        every { LocalData.lastTimeRiskLevelCalculation() } returns System.currentTimeMillis()
        every { LocalData.lastTimeRiskLevelCalculation(any()) } just Runs
        every { LocalData.googleApiToken() } returns UUID.randomUUID().toString()
        every { ConnectivityHelper.isNetworkEnabled(any()) } returns true
        every { CoronaWarnApplication.getAppContext() } returns context
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
                RiskLevelTransaction["executeRiskLevelCalculationDateUpdate"]()
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
                RiskLevelTransaction["executeRiskLevelCalculationDateUpdate"]()
                RiskLevelTransaction["executeClose"]()
            }
        }
    }

    /** Test case for [UNKNOWN_RISK_OUTDATED_RESULTS] if keys are outdated */
    @Test
    fun unknownRiskOutdatedResults() {

        val testRiskLevel = UNKNOWN_RISK_OUTDATED_RESULTS

        val twoHoursAboveMaxStale =
            TimeUnit.HOURS.toMillis(TimeVariables.getMaxStaleExposureRiskRange().plus(2).toLong())

        // tracing is activated
        coEvery { InternalExposureNotificationClient.asyncIsEnabled() } returns true

        // the last time we fetched keys from the server is above the threshold
        every { TimeVariables.getLastTimeDiagnosisKeysFromServerFetch() } returns System.currentTimeMillis()
            .minus(twoHoursAboveMaxStale)

        // active tracing time is 1h above the threshold
        every { TimeVariables.getTimeActiveTracingDuration() } returns TimeUnit.HOURS.toMillis(
            TimeVariables.getMinActivatedTracingTime().plus(1).toLong()
        )

        // background jobs are enabled
        every { ConnectivityHelper.autoModeEnabled(CoronaWarnApplication.getAppContext()) } returns true

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
                RiskLevelTransaction["executeRiskLevelCalculationDateUpdate"]()
                RiskLevelTransaction["executeClose"]()
            }
        }
    }

    /** Test case for [UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL] if keys are outdated and background
     * jobs are disabled */
    @Test
    fun unknownRiskOutdatedResultsManual() {

        val testRiskLevel = UNKNOWN_RISK_OUTDATED_RESULTS_MANUAL

        val twoHoursAboveMaxStale =
            TimeUnit.HOURS.toMillis(TimeVariables.getMaxStaleExposureRiskRange().plus(2).toLong())

        // tracing is activated
        coEvery { InternalExposureNotificationClient.asyncIsEnabled() } returns true

        // the last time we fetched keys from the server is above the threshold
        every { TimeVariables.getLastTimeDiagnosisKeysFromServerFetch() } returns System.currentTimeMillis()
            .minus(twoHoursAboveMaxStale)

        // active tracing time is 1h above the threshold
        every { TimeVariables.getTimeActiveTracingDuration() } returns TimeUnit.HOURS.toMillis(
            TimeVariables.getMinActivatedTracingTime().plus(1).toLong()
        )

        // background jobs are disabled
        every { ConnectivityHelper.autoModeEnabled(CoronaWarnApplication.getAppContext()) } returns false

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
                RiskLevelTransaction["executeRiskLevelCalculationDateUpdate"]()
                RiskLevelTransaction["executeClose"]()
            }
        }
    }

    /** Test case for [INCREASED_RISK]  */
    @Test
    fun increasedRisk() {

        val testRiskLevel = INCREASED_RISK

        val testAppConfig = buildTestAppConfig()

        val testExposureSummary = buildSummary(1600, 0, 30, 15)

        // tracing is activated
        coEvery { InternalExposureNotificationClient.asyncIsEnabled() } returns true

        // the last time we fetched keys from the server happened 30 mins ago (within maxStale)
        every { TimeVariables.getLastTimeDiagnosisKeysFromServerFetch() } returns System.currentTimeMillis()
            .minus(TimeUnit.MINUTES.toMillis(30))

        // We traced only 2h
        every { TimeVariables.getTimeActiveTracingDuration() } returns TimeUnit.HOURS.toMillis(2)

        // the risk score of the last exposure summary is above the high min threshold
        coEvery { ApplicationConfigurationService.asyncRetrieveApplicationConfiguration() } returns testAppConfig
        coEvery { InternalExposureNotificationClient.asyncGetExposureSummary(any()) } returns testExposureSummary

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

                RiskLevelTransaction["executeCheckAppConnectivity"]()

                RiskLevelTransaction["executeRetrieveApplicationConfiguration"]()

                RiskLevelTransaction["executeRetrieveExposureSummary"]()

                RiskLevelTransaction["executeCheckIncreasedRisk"](
                    testAppConfig,
                    testExposureSummary
                )
                RiskLevelTransaction["isValidResult"](testRiskLevel)

                RiskLevelRepository.setRiskLevelScore(testRiskLevel)
                RiskLevelTransaction["executeRiskLevelCalculationDateUpdate"]()
                RiskLevelTransaction["executeClose"]()
            }
        }
    }

    /** Test case for [UNKNOWN_RISK_INITIAL] if tracing threshold is not reached */
    @Test
    fun unknownRiskInitialTracingDuration() {

        val testRiskLevel = UNKNOWN_RISK_INITIAL

        val testAppConfig = buildTestAppConfig()

        val testExposureSummary = buildSummary()

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
        coEvery { ApplicationConfigurationService.asyncRetrieveApplicationConfiguration() } returns testAppConfig
        coEvery { InternalExposureNotificationClient.asyncGetExposureSummary(any()) } returns testExposureSummary

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

                RiskLevelTransaction["executeCheckAppConnectivity"]()

                RiskLevelTransaction["executeRetrieveApplicationConfiguration"]()

                RiskLevelTransaction["executeRetrieveExposureSummary"]()

                RiskLevelTransaction["executeCheckIncreasedRisk"](
                    testAppConfig,
                    testExposureSummary
                )
                RiskLevelTransaction["isValidResult"](UNDETERMINED)

                RiskLevelTransaction["executeCheckUnknownRiskInitialTracingDuration"]()
                RiskLevelTransaction["isValidResult"](testRiskLevel)

                RiskLevelRepository.setRiskLevelScore(testRiskLevel)
                RiskLevelTransaction["executeRiskLevelCalculationDateUpdate"]()
                RiskLevelTransaction["executeClose"]()
            }
        }
    }

    /** Test case for [LOW_LEVEL_RISK] */
    @Test
    fun lowRisk() {

        val testRiskLevel = LOW_LEVEL_RISK

        val testAppConfig = buildTestAppConfig()

        val testExposureSummary = buildSummary(10)

        val twoHoursAboveMinActiveTracingDuration =
            TimeUnit.HOURS.toMillis(TimeVariables.getMinActivatedTracingTime().plus(2).toLong())

        // tracing is activated
        coEvery { InternalExposureNotificationClient.asyncIsEnabled() } returns true

        // the last time we fetched keys from the server happened 30 mins ago (within maxStale)
        every { TimeVariables.getLastTimeDiagnosisKeysFromServerFetch() } returns System.currentTimeMillis()
            .minus(TimeUnit.MINUTES.toMillis(30))

        // the active tracing duration is above the threshold
        every { TimeVariables.getTimeActiveTracingDuration() } returns twoHoursAboveMinActiveTracingDuration

        coEvery { ApplicationConfigurationService.asyncRetrieveApplicationConfiguration() } returns testAppConfig
        coEvery { InternalExposureNotificationClient.asyncGetExposureSummary(any()) } returns testExposureSummary

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

                RiskLevelTransaction["executeCheckAppConnectivity"]()

                RiskLevelTransaction["executeRetrieveApplicationConfiguration"]()

                RiskLevelTransaction["executeRetrieveExposureSummary"]()

                RiskLevelTransaction["executeCheckIncreasedRisk"](
                    testAppConfig,
                    testExposureSummary
                )
                RiskLevelTransaction["isValidResult"](UNDETERMINED)

                RiskLevelTransaction["executeCheckUnknownRiskInitialTracingDuration"]()
                RiskLevelTransaction["isValidResult"](UNDETERMINED)

                RiskLevelRepository.setRiskLevelScore(testRiskLevel)
                RiskLevelTransaction["executeRiskLevelCalculationDateUpdate"]()
                RiskLevelTransaction["executeClose"]()
            }
        }
    }

    /** Test case if app is not connected */
    @Test
    fun checkAppConnectivity() {

        val testRiskLevel = INCREASED_RISK

        // tracing is activated
        coEvery { InternalExposureNotificationClient.asyncIsEnabled() } returns true

        // the last time we fetched keys from the server happened 30 mins ago (within maxStale)
        every { TimeVariables.getLastTimeDiagnosisKeysFromServerFetch() } returns System.currentTimeMillis()
            .minus(TimeUnit.MINUTES.toMillis(30))

        // active tracing time is 1h above the threshold
        every { TimeVariables.getTimeActiveTracingDuration() } returns TimeUnit.HOURS.toMillis(
            TimeVariables.getMinActivatedTracingTime().plus(1).toLong()
        )

        every { RiskLevelRepository.getLastCalculatedScore() } returns testRiskLevel

        every { ConnectivityHelper.isNetworkEnabled(context) } returns false

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

                RiskLevelTransaction["executeCheckAppConnectivity"]()
                RiskLevelRepository.setLastCalculatedRiskLevelAsCurrent()
                RiskLevelTransaction["executeClose"]()
            }
        }
    }

    @After
    fun cleanUp() {
        unmockkAll()
    }

    private fun buildTestAppConfig(
        lowMax: Int = 2749,
        highMin: Int = 2750,
        highMax: Int = 4096
    ): ApplicationConfigurationOuterClass.ApplicationConfiguration {
        return ApplicationConfigurationOuterClass.ApplicationConfiguration
            .newBuilder()
            .setRiskScoreClasses(buildRiskScoreClassification(lowMax, highMin, highMax))
            .setAttenuationDuration(buildAttenuationDuration())
            .build()
    }

    private fun buildAttenuationDuration(): ApplicationConfigurationOuterClass.AttenuationDuration {
        return ApplicationConfigurationOuterClass.AttenuationDuration
            .newBuilder()
            .setRiskScoreNormalizationDivisor(25)
            .setDefaultBucketOffset(0)
            .setThresholds(
                ApplicationConfigurationOuterClass.Thresholds
                    .newBuilder()
                    .setLower(50)
                    .setUpper(70)
                    .build()
            )
            .setWeights(
                ApplicationConfigurationOuterClass.Weights
                    .newBuilder()
                    .setHigh(1.0)
                    .setMid(1.0)
                    .setLow(1.0)
                    .build()
            )
            .build()
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

    private fun buildSummary(
        maxRisk: Int = 0,
        lowAttenuation: Int = 0,
        midAttenuation: Int = 0,
        highAttenuation: Int = 0
    ): ExposureSummary {
        val intArray = IntArray(3)
        intArray[0] = lowAttenuation
        intArray[1] = midAttenuation
        intArray[2] = highAttenuation
        return ExposureSummary.ExposureSummaryBuilder()
            .setMaximumRiskScore(maxRisk)
            .setAttenuationDurations(intArray)
            .build()
    }
}
