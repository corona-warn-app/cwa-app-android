package de.rki.coronawarnapp.appconfig.mapping

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.CoronaPCRTestParametersContainer
import de.rki.coronawarnapp.appconfig.CoronaRapidAntigenTestParametersContainer
import de.rki.coronawarnapp.appconfig.CoronaTestConfig
import de.rki.coronawarnapp.appconfig.CoronaTestConfigContainer
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid.ApplicationConfigurationAndroid
import java.time.Duration
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
            val coronaRapidAntigenTestParameters = coronaTestParameters.coronaRapidAntigenTestParameters
            CoronaRapidAntigenTestParametersContainer(
                hoursToDeemTestOutdated = Duration.ofHours(
                    coronaRapidAntigenTestParameters.hoursToDeemTestOutdated.toLong()
                ),
                durationToShowRiskCard = Duration.ofHours(
                    coronaRapidAntigenTestParameters.hoursSinceSampleCollectionToShowRiskCard.toLong()
                )
            )
        } else {
            Timber.d("coronaRapidAntigenTestParameters is missing")
            CoronaRapidAntigenTestParametersContainer()
        }

        val coronaPCRTestParameters = if (coronaTestParameters.hasCoronaPCRTestParameters()) {
            CoronaPCRTestParametersContainer(
                durationToShowRiskCard = Duration.ofHours(
                    coronaTestParameters.coronaPCRTestParameters.hoursSinceTestRegistrationToShowRiskCard.toLong()
                )
            )
        } else {
            Timber.d("coronaPCRTestParameters is missing")
            CoronaPCRTestParametersContainer()
        }

        return CoronaTestConfigContainer(
            ratParameters = coronaRapidAntigenTestParameters,
            pcrParameters = coronaPCRTestParameters
        )
    }
}
