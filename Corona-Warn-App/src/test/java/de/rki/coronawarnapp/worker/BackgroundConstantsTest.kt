package de.rki.coronawarnapp.worker

import org.junit.Assert
import org.junit.Test

class BackgroundConstantsTest {

    @Test
    fun allBackgroundConstants() {
        Assert.assertEquals(BackgroundConstants.KIND_DELAY, 1L)
        Assert.assertEquals(BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD, 2)
        Assert.assertEquals(BackgroundConstants.POLLING_VALIDITY_MAX_DAYS, 21)
        Assert.assertEquals(BackgroundConstants.BACKOFF_INITIAL_DELAY, 8L)
    }
}
