package de.rki.coronawarnapp.nearby

import org.junit.Assert
import org.junit.Test

class ResolutionRequestCodeConstantsTest {

    @Test
    fun allResolutionRequestCodeConstants() {
        Assert.assertEquals(ResolutionRequestCodeConstants.REQUEST_CODE_START_EXPOSURE_NOTIFICATION_CODE, 1111)
        Assert.assertEquals(ResolutionRequestCodeConstants.REQUEST_CODE_GET_TEMP_EXPOSURE_KEY_HISTORY_CODE, 2222)
    }
}
