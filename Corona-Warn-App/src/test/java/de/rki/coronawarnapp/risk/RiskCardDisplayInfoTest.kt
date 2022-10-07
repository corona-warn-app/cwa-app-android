package de.rki.coronawarnapp.risk

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import java.time.Duration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import testhelpers.BaseTest
import java.time.Instant
import java.util.stream.Stream

@Suppress("MaxLineLength")
internal class RiskCardDisplayInfoTest : BaseTest() {

    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var testRepository: CoronaTestRepository
    @MockK lateinit var timeStamper: TimeStamper

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        val configData = mockk<ConfigData> {
            every { coronaTestParameters } returns mockk {
                every { ratParameters } returns mockk {
                    every { durationToShowRiskCard } returns Duration.ofHours(1)
                }
                every { pcrParameters } returns mockk {
                    every { durationToShowRiskCard } returns Duration.ofHours(1)
                }
            }
        }
        every { appConfigProvider.currentConfig } returns flowOf(configData)
    }

    @ParameterizedTest
    @ArgumentsSource(TestCaseProvider::class)
    fun `shouldShowRiskCard() should return correct result depending on testCase`(testCase: TestCase) =
        runTest {
            with(testCase) {
                every { testRepository.coronaTests } returns flowOf(testSet)
                every { timeStamper.nowUTC } returns Instant.EPOCH.plus(durationPassed)
                createInstance().shouldShowRiskCard(riskState) shouldBe showRiskCardExpectedReturnValue
            }
        }

    @Suppress("LongMethod")
    class TestCaseProvider : ArgumentsProvider {

        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
            return listOf(

                // INCREASED_RISK : Risk card should always be displayed irrespective of the current tests the user has
                TestCase(
                    riskState = RiskState.INCREASED_RISK,
                    testSet = emptySet(),
                    durationPassed = Duration.ZERO,
                    showRiskCardExpectedReturnValue = true
                ),
                TestCase(
                    riskState = RiskState.INCREASED_RISK,
                    testSet = setOf(negativeRatTest),
                    durationPassed = Duration.ZERO,
                    showRiskCardExpectedReturnValue = true
                ),
                TestCase(
                    riskState = RiskState.INCREASED_RISK,
                    testSet = setOf(positivePcrTest),
                    durationPassed = Duration.ZERO,
                    showRiskCardExpectedReturnValue = true
                ),
                TestCase(
                    riskState = RiskState.INCREASED_RISK,
                    testSet = setOf(positiveRatTest),
                    durationPassed = Duration.ZERO,
                    showRiskCardExpectedReturnValue = true
                ),
                TestCase(
                    riskState = RiskState.INCREASED_RISK,
                    testSet = setOf(positiveRatTest, positivePcrTest),
                    durationPassed = Duration.ZERO,
                    showRiskCardExpectedReturnValue = true
                ),

                // LOW_RISK: Risk card should be displayed when the user no positive test
                TestCase(
                    riskState = RiskState.LOW_RISK,
                    testSet = emptySet(),
                    durationPassed = Duration.ZERO,
                    showRiskCardExpectedReturnValue = true
                ),
                TestCase(
                    riskState = RiskState.LOW_RISK,
                    testSet = setOf(negativeRatTest),
                    durationPassed = Duration.ZERO,
                    showRiskCardExpectedReturnValue = true
                ),
                TestCase(
                    riskState = RiskState.LOW_RISK,
                    testSet = setOf(negativePcrTest),
                    durationPassed = Duration.ZERO,
                    showRiskCardExpectedReturnValue = true
                ),
                TestCase(
                    riskState = RiskState.LOW_RISK,
                    testSet = setOf(negativeRatTest, negativePcrTest),
                    durationPassed = Duration.ZERO,
                    showRiskCardExpectedReturnValue = true
                ),

                // LOW_RISK: Risk card should NOT be displayed when the user one or two positive tests and the defined duration has not yet passed
                TestCase(
                    riskState = RiskState.LOW_RISK,
                    testSet = setOf(positiveRatTest),
                    durationPassed = Duration.ofHours(1).minus(Duration.ofSeconds(1)),
                    showRiskCardExpectedReturnValue = false
                ),
                TestCase(
                    riskState = RiskState.LOW_RISK,
                    testSet = setOf(positivePcrTest),
                    durationPassed = Duration.ofHours(1).minus(Duration.ofSeconds(1)),
                    showRiskCardExpectedReturnValue = false
                ),
                TestCase(
                    riskState = RiskState.LOW_RISK,
                    testSet = setOf(positiveRatTest, positivePcrTest),
                    durationPassed = Duration.ofHours(1).minus(Duration.ofSeconds(1)),
                    showRiskCardExpectedReturnValue = false
                ),

                // LOW_RISK: Risk card should be displayed when the user one or two positive tests AND the defined duration has already passed
                TestCase(
                    riskState = RiskState.LOW_RISK,
                    testSet = setOf(positiveRatTest),
                    durationPassed = Duration.ofHours(1),
                    showRiskCardExpectedReturnValue = true
                ),
                TestCase(
                    riskState = RiskState.LOW_RISK,
                    testSet = setOf(positivePcrTest),
                    durationPassed = Duration.ofHours(1),
                    showRiskCardExpectedReturnValue = true
                ),
                TestCase(
                    riskState = RiskState.LOW_RISK,
                    testSet = setOf(positiveRatTest, positivePcrTest),
                    durationPassed = Duration.ofHours(1),
                    showRiskCardExpectedReturnValue = true
                ),

                // CALCULATION_FAILED: Risk card should be always displayed except when the user has at least one positive test and the duration has not yet passed(same as LOW_RISK)
                TestCase(
                    riskState = RiskState.CALCULATION_FAILED,
                    testSet = emptySet(),
                    durationPassed = Duration.ZERO,
                    showRiskCardExpectedReturnValue = true
                ),
                TestCase(
                    riskState = RiskState.CALCULATION_FAILED,
                    testSet = setOf(positivePcrTest),
                    durationPassed = Duration.ofHours(1).minus(Duration.ofSeconds(1)),
                    showRiskCardExpectedReturnValue = false
                ),
                TestCase(
                    riskState = RiskState.CALCULATION_FAILED,
                    testSet = setOf(positiveRatTest, positivePcrTest),
                    durationPassed = Duration.ofHours(1),
                    showRiskCardExpectedReturnValue = true
                ),

                // RISK-STATE = null (tracing disabled or calculation currently in progress) same behaviour as with states LOW_RISK and CALCULATION_FAILED
                TestCase(
                    riskState = null,
                    testSet = emptySet(),
                    durationPassed = Duration.ZERO,
                    showRiskCardExpectedReturnValue = true
                ),
                TestCase(
                    riskState = null,
                    testSet = setOf(positivePcrTest),
                    durationPassed = Duration.ofHours(1).minus(Duration.ofSeconds(1)),
                    showRiskCardExpectedReturnValue = false
                ),
                TestCase(
                    riskState = null,
                    testSet = setOf(positiveRatTest, positivePcrTest),
                    durationPassed = Duration.ofHours(1),
                    showRiskCardExpectedReturnValue = true
                ),
            ).map { Arguments.of(it) }.stream()
        }

        private val negativeRatTest = mockk<RACoronaTest> {
            every { type } returns BaseCoronaTest.Type.RAPID_ANTIGEN
            every { isPositive } returns false
            every { isViewed } returns false
        }

        private val negativePcrTest = mockk<PCRCoronaTest> {
            every { type } returns BaseCoronaTest.Type.PCR
            every { isPositive } returns false
            every { isViewed } returns false
        }

        private val positiveRatTest = mockk<RACoronaTest> {
            every { type } returns BaseCoronaTest.Type.RAPID_ANTIGEN
            every { isPositive } returns true
            every { isViewed } returns true
            every { testTakenAt } returns Instant.EPOCH
        }

        private val positivePcrTest = mockk<PCRCoronaTest> {
            every { type } returns BaseCoronaTest.Type.PCR
            every { isPositive } returns true
            every { isViewed } returns true
            every { registeredAt } returns Instant.EPOCH
        }
    }

    private fun createInstance() = RiskCardDisplayInfo(
        appConfigProvider,
        testRepository,
        timeStamper
    )

    data class TestCase(
        val riskState: RiskState?,
        val testSet: Set<PersonalCoronaTest>,
        val durationPassed: Duration,
        val showRiskCardExpectedReturnValue: Boolean
    )
}
