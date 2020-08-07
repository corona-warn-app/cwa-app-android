package de.rki.coronawarnapp.service.diagnosiskey

import org.junit.Assert
import org.junit.Test

class DiagnosisKeyConstantsTest {

    @Test
    fun allDiagnosisKeyConstants() {
        Assert.assertEquals(DiagnosisKeyConstants.HOUR, "hour")
        Assert.assertEquals(DiagnosisKeyConstants.SERVER_ERROR_CODE_403, 403)
        Assert.assertEquals(DiagnosisKeyConstants.INDEX_DOWNLOAD_URL, "version/v1/index.txt")
        Assert.assertEquals(DiagnosisKeyConstants.DIAGNOSIS_KEYS_DOWNLOAD_URL, "version/v1/diagnosis-keys")
        Assert.assertEquals(DiagnosisKeyConstants.DIAGNOSIS_KEYS_SUBMISSION_URL, "version/v1/diagnosis-keys")
        Assert.assertEquals(DiagnosisKeyConstants.PARAMETERS_COUNTRY_DOWNLOAD_URL, "version/v1/parameters/country")
        Assert.assertEquals(DiagnosisKeyConstants.APPCONFIG_COUNTRY_DOWNLOAD_URL, "version/v1/configuration/country")
        Assert.assertEquals(
            DiagnosisKeyConstants.COUNTRY_APPCONFIG_DOWNLOAD_URL,
            "version/v1/configuration/country/DE/app_config"
        )
        Assert.assertEquals(DiagnosisKeyConstants.AVAILABLE_DATES_URL, "version/v1/diagnosis-keys/country/DE/date")
    }
}
