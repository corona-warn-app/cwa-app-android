package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.server.protocols.internal.v2.PresenceTracingParametersOuterClass
.PresenceTracingPlausibleDeniabilityParameters.NumberOfFakeCheckInsFunctionParametersOrBuilder
import de.rki.coronawarnapp.server.protocols.internal.v2.PresenceTracingParametersOuterClass
.PresenceTracingPlausibleDeniabilityParameters.NumberOfFakeCheckInsFunctionParameters
import de.rki.coronawarnapp.util.PaddingTool.equation
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import testhelpers.BaseTest
import timber.log.Timber
import java.util.stream.Stream
import kotlin.math.abs
import kotlin.random.Random

class PaddingToolTest : BaseTest() {

    private val validPattern = "^([A-Za-z0-9]+)$".toRegex()

    @Test
    fun `verify padding patterns`() {
        repeat(1000) {
            val randomLength = abs(Random.nextInt(1, 1024))
            PaddingTool.requestPadding(randomLength).apply {
                length shouldBe randomLength
                Timber.v("RandomLength: %d, Padding: %s", randomLength, this)
                validPattern.matches(this) shouldBe true
            }
        }
    }

    @Test
    fun `keyPadding - fake requests with 0 keys`() {
        // keyPadding = 14 keys x 28 bytes per key = 392 bytes`
        PaddingTool.keyPadding(keyListSize = 0).toByteArray().size shouldBe 392
    }

    @Test
    fun `keyPadding - genuine request with 5 keys`() {
        // keyPadding = 9 keys x 28 bytes per key = 252 bytes`
        PaddingTool.keyPadding(keyListSize = 5).toByteArray().size shouldBe 252
    }

    @Test
    fun `keyPadding - genuine request with 16 keys`() {
        //keyPadding = 0 keys x 28 bytes per key = 0 bytes`
        PaddingTool.keyPadding(keyListSize = 16).toByteArray().size shouldBe 0
    }

    @ParameterizedTest
    @ArgumentsSource(EquationProvider::class)
    fun `f(x)`(x: Double, fx: Double) {
        /*
          "parameters": {
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
        val functionParam: NumberOfFakeCheckInsFunctionParametersOrBuilder =
            NumberOfFakeCheckInsFunctionParameters.newBuilder()
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

        functionParam.equation(x) shouldBe fx
    }
}

/*
  "data": [
        { "x": -1, "fx": 26.0308204914619 },
        { "x": 0, "fx": 61.59927895860675 },
        { "x": 1, "fx": 94.75879170760312 }
      ]
 */
class EquationProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> =
        Stream.of(
            Arguments.of(-1.0, 26.03082049146190),
            Arguments.of( 0.0, 61.59927895860675),
            Arguments.of( 1.0, 94.75879170760312)
        )
}
