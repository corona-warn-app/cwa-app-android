package de.rki.coronawarnapp.service.applicationconfiguration

import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.server.protocols.ApplicationConfigurationOuterClass
import de.rki.coronawarnapp.service.diagnosiskey.DiagnosisKeyConstants

object ApplicationConfigurationService {
    suspend fun asyncRetrieveApplicationConfiguration(): ApplicationConfigurationOuterClass.ApplicationConfiguration {
        return WebRequestBuilder
            .asyncGetApplicationConfigurationFromServer(DiagnosisKeyConstants.COUNTRY_APPCONFIG_DOWNLOAD_URL)
    }
}
