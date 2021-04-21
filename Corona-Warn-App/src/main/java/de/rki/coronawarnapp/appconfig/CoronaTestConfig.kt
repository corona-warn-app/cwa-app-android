package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.mapping.ConfigMapper

interface CoronaTestConfig {
    val coronaRapidAntigenTestParameters: CoronaRapidAntigenTestParametersContainer

    interface Mapper : ConfigMapper<CoronaTestConfig>
}

data class CoronaRapidAntigenTestParametersContainer(
    val hoursToDeemTestOutdated: Long = DEFAULT_HOURS
) {
    companion object {
        const val DEFAULT_HOURS = 48L
    }
}

data class CoronaTestConfigContainer(
    override val coronaRapidAntigenTestParameters: CoronaRapidAntigenTestParametersContainer =
        CoronaRapidAntigenTestParametersContainer()
) : CoronaTestConfig
