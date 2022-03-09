package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.mapping.ConfigMapper
import org.joda.time.Duration

interface CoronaTestConfig {
    val ratParameters: CoronaRapidAntigenTestParametersContainer
    val pcrParameters: CoronaPCRTestParametersContainer

    interface Mapper : ConfigMapper<CoronaTestConfig>
}

data class CoronaRapidAntigenTestParametersContainer(
    val hoursToDeemTestOutdated: Duration = Duration.standardHours(DEFAULT_HOURS),
    val hoursToShowRiskCard: Duration = Duration.standardHours(DEFAULT_HOURS_SINCE_SAMPLE_COLLECTION),
) {
    companion object {
        const val DEFAULT_HOURS: Long = 48
        const val DEFAULT_HOURS_SINCE_SAMPLE_COLLECTION: Long = 168L // 7 days x 24 hours
    }
}

data class CoronaPCRTestParametersContainer(
    val hoursToShowRiskCard: Duration = Duration.standardHours(DEFAULT_HOURS_SINCE_TEST_REGISTRATION),
) {
    companion object {
        const val DEFAULT_HOURS_SINCE_TEST_REGISTRATION = 168L // 7 days x 24 hours
    }
}

data class CoronaTestConfigContainer(
    override val ratParameters: CoronaRapidAntigenTestParametersContainer = CoronaRapidAntigenTestParametersContainer(),
    override val pcrParameters: CoronaPCRTestParametersContainer = CoronaPCRTestParametersContainer()
) : CoronaTestConfig
