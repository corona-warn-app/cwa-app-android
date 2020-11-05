package de.rki.coronawarnapp.appconfig.mapping

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.RiskCalculationConfig
import de.rki.coronawarnapp.server.protocols.internal.AppConfig
import de.rki.coronawarnapp.server.protocols.internal.AttenuationDurationOuterClass
import de.rki.coronawarnapp.server.protocols.internal.RiskScoreClassificationOuterClass
import javax.inject.Inject

@Reusable
class RiskCalculationConfigMapper @Inject constructor() : RiskCalculationConfig.Mapper {

    override fun map(rawConfig: AppConfig.ApplicationConfiguration): RiskCalculationConfig {
        return RiskCalculationContainer(
            minRiskScore = rawConfig.minRiskScore,
            riskScoreClasses = rawConfig.riskScoreClasses,
            attenuationDuration = rawConfig.attenuationDuration
        )
    }

    data class RiskCalculationContainer(
        override val minRiskScore: Int,
        override val attenuationDuration: AttenuationDurationOuterClass.AttenuationDuration,
        override val riskScoreClasses: RiskScoreClassificationOuterClass.RiskScoreClassification
    ) : RiskCalculationConfig
}
