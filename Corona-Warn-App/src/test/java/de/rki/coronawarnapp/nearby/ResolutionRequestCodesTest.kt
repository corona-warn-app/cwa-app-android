package de.rki.coronawarnapp.nearby

import org.junit.Assert
import org.junit.Test

class ResolutionRequestCodesTest {

    @Test
    fun allResolutionRequestCodes() {
        Assert.assertEquals(
            ResolutionRequestCodes.REQUEST_CODE_START_EXPOSURE_NOTIFICATION.code,
            ResolutionRequestCodeConstants.REQUEST_CODE_START_EXPOSURE_NOTIFICATION_CODE
        )
        Assert.assertEquals(
            ResolutionRequestCodes.REQUEST_CODE_GET_TEMP_EXPOSURE_KEY_HISTORY.code,
            ResolutionRequestCodeConstants.REQUEST_CODE_GET_TEMP_EXPOSURE_KEY_HISTORY_CODE
        )
    }
}
