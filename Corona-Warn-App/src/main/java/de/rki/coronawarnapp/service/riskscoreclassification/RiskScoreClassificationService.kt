package de.rki.coronawarnapp.service.riskscoreclassification

import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass.RiskScoreClassification

object RiskScoreClassificationService {
    suspend fun asyncRetrieveRiskScoreClassification(): RiskScoreClassification {
        return WebRequestBuilder
            .asyncGetApplicationConfigurationFromServer()
            .riskScoreClasses
    }
}
