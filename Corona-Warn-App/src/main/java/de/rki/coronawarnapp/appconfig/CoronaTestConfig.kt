package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.mapping.ConfigMapper
import org.joda.time.Duration

interface CoronaTestConfig {
    val coronaRapidAntigenTestParameters: CoronaRapidAntigenTestParametersContainer

    interface Mapper : ConfigMapper<CoronaTestConfig>
}

data class CoronaRapidAntigenTestParametersContainer(
    val hoursToDeemTestOutdated: Duration = Duration.standardHours(DEFAULT_HOURS)
) {
    companion object {
        const val DEFAULT_HOURS: Long = 48
    }
}

data class CoronaTestConfigContainer(
    override val coronaRapidAntigenTestParameters: CoronaRapidAntigenTestParametersContainer =
        CoronaRapidAntigenTestParametersContainer()
) : CoronaTestConfig
