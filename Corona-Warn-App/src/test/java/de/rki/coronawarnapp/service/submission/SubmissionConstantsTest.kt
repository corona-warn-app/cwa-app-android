package de.rki.coronawarnapp.service.submission

import org.junit.Assert
import org.junit.Test

class SubmissionConstantsTest {

    @Test
    fun allSubmissionConstants() {
        Assert.assertEquals(SubmissionConstants.QR_CODE_KEY_TYPE, "GUID")
        Assert.assertEquals(SubmissionConstants.TELE_TAN_KEY_TYPE, "TELETAN")
        Assert.assertEquals(SubmissionConstants.REGISTRATION_TOKEN_URL, "version/v1/registrationToken")
        Assert.assertEquals(SubmissionConstants.TEST_RESULT_URL, "version/v1/testresult")
        Assert.assertEquals(SubmissionConstants.TAN_REQUEST_URL, "version/v1/tan")

        Assert.assertEquals(SubmissionConstants.MAX_QR_CODE_LENGTH, 150)
        Assert.assertEquals(SubmissionConstants.MAX_GUID_LENGTH, 80)
        Assert.assertEquals(SubmissionConstants.GUID_SEPARATOR, '?')

        Assert.assertEquals(SubmissionConstants.SERVER_ERROR_CODE_400, 400)
    }
}
