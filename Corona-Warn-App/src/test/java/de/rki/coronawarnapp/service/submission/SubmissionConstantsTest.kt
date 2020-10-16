package de.rki.coronawarnapp.service.submission

import org.junit.Assert
import org.junit.Test

class SubmissionConstantsTest {

    @Test
    fun allSubmissionConstants() {
        Assert.assertEquals(QRScanResult.MAX_QR_CODE_LENGTH, 150)
        Assert.assertEquals(QRScanResult.MAX_GUID_LENGTH, 80)
        Assert.assertEquals(QRScanResult.GUID_SEPARATOR, '?')
    }
}
