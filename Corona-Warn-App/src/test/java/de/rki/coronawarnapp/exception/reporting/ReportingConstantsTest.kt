package de.rki.coronawarnapp.exception.reporting

import org.junit.Assert
import org.junit.Test

class ReportingConstantsTest {

    @Test
    fun allReportingConstants() {
        Assert.assertEquals(ReportingConstants.ERROR_REPORT_LOCAL_BROADCAST_CHANNEL, "error-report")
        Assert.assertEquals(ReportingConstants.ERROR_REPORT_CATEGORY_EXTRA, "category")
        Assert.assertEquals(ReportingConstants.ERROR_REPORT_PREFIX_EXTRA, "prefix")
        Assert.assertEquals(ReportingConstants.ERROR_REPORT_SUFFIX_EXTRA, "suffix")
        Assert.assertEquals(ReportingConstants.ERROR_REPORT_MESSAGE_EXTRA, "message")
        Assert.assertEquals(ReportingConstants.ERROR_REPORT_STACK_EXTRA, "stack")
        Assert.assertEquals(ReportingConstants.ERROR_REPORT_CODE_EXTRA, "code")
        Assert.assertEquals(
            ReportingConstants.ERROR_REPORT_API_EXCEPTION_CODE,
            "api-exception-code"
        )
        Assert.assertEquals(ReportingConstants.ERROR_REPORT_RES_ID, "res-id")
        Assert.assertEquals(ReportingConstants.STATUS_CODE_GOOGLE_UPDATE_NEEDED, 17)
        Assert.assertEquals(ReportingConstants.STATUS_CODE_REACHED_REQUEST_LIMIT, 39508)
        Assert.assertEquals(ReportingConstants.STATUS_CODE_GOOGLE_API_FAIL, 10)
    }
}
