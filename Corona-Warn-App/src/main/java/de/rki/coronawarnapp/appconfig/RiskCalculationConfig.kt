package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.mapping.ConfigMapper
import de.rki.coronawarnapp.server.protocols.internal.AttenuationDurationOuterClass
import de.rki.coronawarnapp.server.protocols.internal.RiskScoreClassificationOuterClass

interface RiskCalculationConfig {

    val minRiskScore: Int

    val attenuationDuration: AttenuationDurationOuterClass.AttenuationDuration

    val riskScoreClasses: RiskScoreClassificationOuterClass.RiskScoreClassification

    interface Mapper : ConfigMapper<RiskCalculationConfig>
}
