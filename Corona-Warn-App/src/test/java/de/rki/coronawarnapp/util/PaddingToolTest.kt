package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.appconfig.PlausibleDeniabilityParametersContainer
import de.rki.coronawarnapp.server.protocols.internal.v2.PresenceTracingParametersOuterClass.PresenceTracingPlausibleDeniabilityParameters.NumberOfFakeCheckInsFunctionParameters
import de.rki.coronawarnapp.server.protocols.internal.v2.PresenceTracingParametersOuterClass.PresenceTracingPlausibleDeniabilityParameters.NumberOfFakeCheckInsFunctionParametersOrBuilder
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass.Range
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import testhelpers.BaseTest
import timber.log.Timber
import java.security.SecureRandom
import kotlin.math.abs
import kotlin.random.Random
import kotlin.random.asKotlinRandom

class PaddingToolTest : BaseTest() {

    private val validPattern = "^([A-Za-z0-9]+)$".toRegex()

    /*
      "parameters": {
      "randomNumberRange": { "min": -9999, "max": 9999 },
        "p": 100,
        "q": 1.4,
        "r": -1,
        "s": 0.8,
        "t": -1.5,
        "u": 2,
        "a": 0,
        "b": 0,
        "c": 0
      }
     */
    private val functionParam: NumberOfFakeCheckInsFunctionParametersOrBuilder =
        NumberOfFakeCheckInsFunctionParameters.newBuilder()
            .setRandomNumberRange(
                Range.newBuilder()
                    .setMax(9999.0)
                    .setMin(-9999.0)
            )
            .setP(100.0)
            .setQ(1.4)
            .setR(-1.0)
            .setS(0.8)
            .setT(-1.5)
            .setU(2.0)
            .setA(0.0)
            .setB(0.0)
            .setC(0.0)
            .build()

    private fun createInstance() = PaddingTool(
        sourceFast = SecureRandom().asKotlinRandom(),
    )

    @Test
    fun `verify padding patterns`() {
        repeat(1000) {
            val randomLength = abs(Random.nextInt(1, 1024))
            createInstance().requestPadding(randomLength).apply {
                length shouldBe randomLength
                Timber.v("RandomLength: %d, Padding: %s", randomLength, this)
                validPattern.matches(this) shouldBe true
            }
        }
    }

    @Test
    fun `keyPadding - fake requests with 0 keys`() {
        // keyPadding = 15 keys x 28 bytes per key = 392 bytes`
        createInstance().keyPadding(keyListSize = 0).toByteArray().size shouldBe 420
    }

    @Test
    fun `srs request padding`() {
        createInstance().srsAuthPadding(0, 0).isEmpty() shouldBe true
        createInstance().srsAuthPadding(19, 19).isEmpty() shouldBe true
        createInstance().srsAuthPadding(45, 19).isEmpty() shouldBe true

        (createInstance().srsAuthPadding(19, 45).size in 19..45) shouldBe true
        (createInstance().srsAuthPadding(1, 100).size in 1..100) shouldBe true
    }

    @Test
    fun `keyPadding - genuine request with 5 keys`() {
        // keyPadding = 10 keys x 28 bytes per key = 252 bytes`
        createInstance().keyPadding(keyListSize = 5).toByteArray().size shouldBe 280
    }

    @Test
    fun `keyPadding - genuine request with 16 keys`() {
        // keyPadding = 0 keys x 28 bytes per key = 0 bytes`
        createInstance().keyPadding(keyListSize = 16).toByteArray().size shouldBe 0
    }

    @Test
    fun `checkInPadding - average test`() {
        /*
          {
          "testCases": [
            {
              "description": "returns on average a byte sequence with a certain length",
              "numberOfRuns": 1000,
              "parameters": {
                "numberOfCheckInsInDatabaseTable": 0,
                "appConfigParameters": {
                  "checkInSizesInBytes": [ 10 ],
                  "probabilityToFakeCheckInsIfSomeCheckIns": 1,
                  "probabilityToFakeCheckInsIfNoCheckIns": 1,
                  "numberOfFakeCheckInsFunctionParameters": [
                    {
                      "randomNumberRange": { "min": -9999, "max": 9999 },
                      "p": 100,
                      "q": 1.4,
                      "r": -1,
                      "s": 0.8,
                      "t": -1.5,
                      "u": 2,
                      "a": 0,
                      "b": 0,
                      "c": 0
                    }
                  ]
                }
              },
              "expMinAvgCheckInPadding": 550,
              "expMaxAvgCheckInPadding": 650
            }
          ]
        }
       */
        val plausibleParameters = PlausibleDeniabilityParametersContainer(
            checkInSizesInBytes = listOf(10),
            probabilityToFakeCheckInsIfSomeCheckIns = 1.0,
            probabilityToFakeCheckInsIfNoCheckIns = 1.0,
            numberOfFakeCheckInsFunctionParameters = listOf(functionParam)
        )
        val numberOfRuns = 1000
        val expMinAvgCheckInPadding = 550
        val expMaxAvgCheckInPadding = 650
        val instance = createInstance()
        val byteAvg = (0 until numberOfRuns)
            .map {
                instance.checkInPadding(
                    plausibleParameters,
                    checkInListSize = 0
                ).length // Generated Random Padding (String)  length
            }
            .fold(initial = 0) { sum, length ->
                sum + length
            } / numberOfRuns

        byteAvg shouldBeGreaterThanOrEqual expMinAvgCheckInPadding
        byteAvg shouldBeLessThanOrEqual expMaxAvgCheckInPadding
    }

    @ParameterizedTest
    @ArgumentsSource(EquationProvider::class)
    fun `f(x)`(x: Double, fx: Double) {
        functionParam.fakeCheckinCountEquation(x) shouldBe fx
    }

    @ParameterizedTest
    @ArgumentsSource(ZeroFakeCheckInsProvider::class)
    fun `Determine Number Of Fake CheckIns = 0`(
        plausibleParameters: PlausibleDeniabilityParametersContainer,
        numberOfCheckIns: Int,
        expected: Double
    ) {
        createInstance().determineFakeCheckInsNumber(plausibleParameters, numberOfCheckIns) shouldBe expected
    }

    @ParameterizedTest
    @ArgumentsSource(MoreThanZeroFakeCheckInsProvider::class)
    fun `Determine Number Of Fake CheckIns more than 0`(
        plausibleParameters: PlausibleDeniabilityParametersContainer,
        numberOfCheckIns: Int,
        expected: Double
    ) {
        createInstance().determineFakeCheckInsNumber(
            plausibleParameters,
            numberOfCheckIns
        ) shouldBeGreaterThan expected
    }
}
