package de.rki.coronawarnapp.service.diagnosiskey

import org.junit.Assert
import org.junit.Test

class DiagnosisKeyConstantsTest {

    @Test
    fun allDiagnosisKeyConstants() {
        Assert.assertEquals(DiagnosisKeyConstants.SERVER_ERROR_CODE_403, 403)
        Assert.assertEquals(DiagnosisKeyConstants.DIAGNOSIS_KEYS_SUBMISSION_URL, "version/v1/diagnosis-keys")
    }
}
