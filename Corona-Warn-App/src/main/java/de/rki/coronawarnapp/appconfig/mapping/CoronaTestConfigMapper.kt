package de.rki.coronawarnapp.appconfig.mapping

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.CoronaRapidAntigenTestParametersContainer
import de.rki.coronawarnapp.appconfig.CoronaTestConfig
import de.rki.coronawarnapp.appconfig.CoronaTestConfigContainer
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid.ApplicationConfigurationAndroid
import org.joda.time.Duration
import timber.log.Timber
import javax.inject.Inject

@Reusable
class CoronaTestConfigMapper @Inject constructor() : CoronaTestConfig.Mapper {
    override fun map(
        rawConfig: ApplicationConfigurationAndroid
    ): CoronaTestConfig {

        if (!rawConfig.hasCoronaTestParameters()) {
            Timber.d("coronaTestParameters are missing")
            return CoronaTestConfigContainer()
        }

        return rawConfig.mapCoronaTestParameters()
    }

    private fun ApplicationConfigurationAndroid.mapCoronaTestParameters(): CoronaTestConfig {
        val coronaRapidAntigenTestParameters = if (coronaTestParameters.hasCoronaRapidAntigenTestParameters()) {
            CoronaRapidAntigenTestParametersContainer(
                Duration.standardHours(
                    coronaTestParameters.coronaRapidAntigenTestParameters.hoursToDeemTestOutdated.toLong()
                )
            )
        } else {
            Timber.d("coronaRapidAntigenTestParameters is missing")
            CoronaRapidAntigenTestParametersContainer()
        }
        return CoronaTestConfigContainer(coronaRapidAntigenTestParameters)
    }
}
