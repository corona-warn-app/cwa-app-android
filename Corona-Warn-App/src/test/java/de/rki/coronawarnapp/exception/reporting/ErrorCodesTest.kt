package de.rki.coronawarnapp.exception.reporting

import org.junit.Assert
import org.junit.Test

class ErrorCodesTest {

    @Test
    fun allErrorCodes() {
        // TECHNICAL
        Assert.assertEquals(ErrorCodes.WRONG_RECEIVER_PROBLEM.code, 100)
        Assert.assertEquals(ErrorCodes.TRANSACTION_PROBLEM.code, 500)
        Assert.assertEquals(ErrorCodes.ROLLBACK_PROBLEM.code, 510)
        Assert.assertEquals(ErrorCodes.APPLICATION_CONFIGURATION_CORRUPT.code, 1000)
        Assert.assertEquals(ErrorCodes.APPLICATION_CONFIGURATION_INVALID.code, 1001)
        Assert.assertEquals(ErrorCodes.CWA_SECURITY_PROBLEM.code, 2000)
        Assert.assertEquals(ErrorCodes.CWA_WEB_SECURITY_PROBLEM.code, 2001)
        Assert.assertEquals(ErrorCodes.DIAGNOSIS_KEY_SERVICE_PROBLEM.code, 3000)
        Assert.assertEquals(ErrorCodes.RISK_LEVEL_CALCULATION_PROBLEM.code, 3500)
        Assert.assertEquals(ErrorCodes.CWA_WEB_REQUEST_PROBLEM.code, 4000)
        Assert.assertEquals(ErrorCodes.EN_PERMISSION_PROBLEM.code, 7000)
        Assert.assertEquals(ErrorCodes.FORMATTER_PROBLEM.code, 8000)
        Assert.assertEquals(ErrorCodes.REPORTED_EXCEPTION_PROBLEM.code, 9001)
        Assert.assertEquals(ErrorCodes.REPORTED_IO_EXCEPTION_PROBLEM.code, 9101)
        Assert.assertEquals(ErrorCodes.REPORTED_EXCEPTION_UNKNOWN_PROBLEM.code, 9002)

        // NONTECHNICAL
        Assert.assertEquals(ErrorCodes.NO_NETWORK_CONNECTIVITY.code, 1)
        Assert.assertEquals(ErrorCodes.NOT_ENOUGH_AVAILABLE_SPACE_ON_DISK.code, 2)
        Assert.assertEquals(ErrorCodes.API_EXCEPTION.code, 3)
        Assert.assertEquals(ErrorCodes.EXTERNAL_NAVIGATION.code, 10)
    }
}
