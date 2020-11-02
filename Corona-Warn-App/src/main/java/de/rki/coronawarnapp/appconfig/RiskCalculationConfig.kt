package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.server.protocols.internal.AppConfig.ApplicationConfiguration
import de.rki.coronawarnapp.server.protocols.internal.AttenuationDurationOuterClass
import de.rki.coronawarnapp.server.protocols.internal.RiskScoreClassificationOuterClass

interface RiskCalculationConfig {

    val minRiskScore: Int

    val attenuationDuration: AttenuationDurationOuterClass.AttenuationDuration

    val riskScoreClasses: RiskScoreClassificationOuterClass.RiskScoreClassification

    interface Mapper {
        fun map(rawConfig: ApplicationConfiguration): RiskCalculationConfig
    }
}
