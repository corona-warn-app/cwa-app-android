package de.rki.coronawarnapp.worker

import org.junit.Assert
import org.junit.Test

class BackgroundConstantsTest {

    @Test
    fun allBackgroundConstants() {
        Assert.assertEquals(BackgroundConstants.MINUTES_IN_DAY, 1440)
        Assert.assertEquals(BackgroundConstants.DIAGNOSIS_KEY_RETRIEVAL_TRIES_PER_DAY, 12)
        Assert.assertEquals(BackgroundConstants.GOOGLE_API_MAX_CALLS_PER_DAY, 20)
        Assert.assertEquals(BackgroundConstants.DIAGNOSIS_TEST_RESULT_RETRIEVAL_TRIES_PER_DAY, 12)
        Assert.assertEquals(BackgroundConstants.KIND_DELAY, 1L)
        Assert.assertEquals(BackgroundConstants.DIAGNOSIS_TEST_RESULT_PERIODIC_INITIAL_DELAY, 10L)
        Assert.assertEquals(BackgroundConstants.WORKER_RETRY_COUNT_THRESHOLD, 2)
        Assert.assertEquals(BackgroundConstants.POLLING_VALIDITY_MAX_DAYS, 21)
        Assert.assertEquals(BackgroundConstants.BACKOFF_INITIAL_DELAY, 8L)
    }
}
