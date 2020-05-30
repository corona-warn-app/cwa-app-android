package de.rki.coronawarnapp.service.riskscoreclassification

import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass.RiskScoreClassification
import de.rki.coronawarnapp.service.diagnosiskey.DiagnosisKeyConstants

object RiskScoreClassificationService {
    suspend fun asyncRetrieveRiskScoreClassification(): RiskScoreClassification {
        return WebRequestBuilder
            .asyncGetApplicationConfigurationFromServer(DiagnosisKeyConstants.COUNTRY_APPCONFIG_DOWNLOAD_URL)
            .riskScoreClasses
    }
}
