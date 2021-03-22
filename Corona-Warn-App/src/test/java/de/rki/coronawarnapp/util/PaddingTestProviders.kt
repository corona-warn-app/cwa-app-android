package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.appconfig.PlausibleDeniabilityParametersContainer
import de.rki.coronawarnapp.server.protocols.internal.v2.PresenceTracingParametersOuterClass
.PresenceTracingPlausibleDeniabilityParameters.NumberOfFakeCheckInsFunctionParameters
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass.Range
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream

class EquationProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> =
        /*
          "data": [
                { "x": -1, "fx": 26.0308204914619 },
                { "x": 0, "fx": 61.59927895860675 },
                { "x": 1, "fx": 94.75879170760312 }
              ]
         */
        Stream.of(
            Arguments.of(-1.0, 26.03082049146190),
            Arguments.of(0.0, 61.59927895860675),
            Arguments.of(1.0, 94.75879170760312)
        )
}

class ZeroFakeCheckInsProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> =
        Stream.of(
            /*
              {
              "description": "returns 0 if there are no check-ins and the respective probability is 0",
              "parameters": {
                "numberOfCheckInsInDatabaseTable": 0,
                "appConfigParameters": {
                  "probabilityToFakeCheckInsIfSomeCheckIns": 1,
                  "probabilityToFakeCheckInsIfNoCheckIns": 0,
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
              "expNumberOfCheckIns": 0
            }
           */
            Arguments.of(
                PlausibleDeniabilityParametersContainer(
                    probabilityToFakeCheckInsIfSomeCheckIns = 1.0,
                    probabilityToFakeCheckInsIfNoCheckIns = 0.0,
                    numberOfFakeCheckInsFunctionParameters = functionParamsList()
                ),
                0, // Number of CheckIns in Database
                0 // Expected number of fake CheckIns
            ),

            /*
                {
                  "description": "returns 0 if there are some check-ins and the respective probability is 0",
                  "parameters": {
                    "numberOfCheckInsInDatabaseTable": 10,
                    "appConfigParameters": {
                      "probabilityToFakeCheckInsIfSomeCheckIns": 0,
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
                  "expNumberOfCheckIns": 0
                }
             */
            Arguments.of(
                PlausibleDeniabilityParametersContainer(
                    probabilityToFakeCheckInsIfSomeCheckIns = 0.0,
                    probabilityToFakeCheckInsIfNoCheckIns = 1.0,
                    numberOfFakeCheckInsFunctionParameters = functionParamsList()
                ),
                10, // Number of CheckIns in Database
                0 // Expected number of fake CheckIns
            )
        )
}

class MoreThanZeroFakeCheckInsProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> =
        Stream.of(
            /*
              {
                  "description": "returns more than 0 if there are no check-ins and the respective probability is 1",
                  "parameters": {
                    "numberOfCheckInsInDatabaseTable": 0,
                    "appConfigParameters": {
                      "probabilityToFakeCheckInsIfSomeCheckIns": 0,
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
                  "expNumberOfCheckInsMoreThan": 0
              }
             */
            Arguments.of(
                PlausibleDeniabilityParametersContainer(
                    probabilityToFakeCheckInsIfSomeCheckIns = 0.0,
                    probabilityToFakeCheckInsIfNoCheckIns = 1.0,
                    numberOfFakeCheckInsFunctionParameters = functionParamsList()
                ),
                0, // Number of CheckIns in Database
                0 // Expected number of fake CheckIns more than 0
            ),

            /*
              {
                  "description": "returns more than 0 if there are some check-ins and the respective probability is 1",
                  "parameters": {
                    "numberOfCheckInsInDatabaseTable": 10,
                    "appConfigParameters": {
                      "probabilityToFakeCheckInsIfSomeCheckIns": 1,
                      "probabilityToFakeCheckInsIfNoCheckIns": 0,
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
                  "expNumberOfCheckInsMoreThan": 0
              }
             */
            Arguments.of(
                PlausibleDeniabilityParametersContainer(
                    probabilityToFakeCheckInsIfSomeCheckIns = 1.0,
                    probabilityToFakeCheckInsIfNoCheckIns = 0.0,
                    numberOfFakeCheckInsFunctionParameters = functionParamsList()
                ),
                10, // Number of CheckIns in Database
                0 // Expected number of fake CheckIns more than 0
            )
        )
}

private fun functionParamsList() = listOf(
    NumberOfFakeCheckInsFunctionParameters.newBuilder()
        .setRandomNumberRange(
            Range.newBuilder()
                .setMin(-9999.0)
                .setMax(9999.0)
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
)
