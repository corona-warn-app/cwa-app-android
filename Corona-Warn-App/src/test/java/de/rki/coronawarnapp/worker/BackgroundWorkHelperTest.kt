package de.rki.coronawarnapp.worker

import androidx.work.NetworkType
import org.junit.Assert
import org.junit.Test

class BackgroundWorkHelperTest {
    @Test
    fun getDiagnosisKeyRetrievalPeriodicWorkTimeInterval() {
        Assert.assertEquals(
            BackgroundWorkHelper.getDiagnosisKeyRetrievalPeriodicWorkTimeInterval(),
            120
        )
    }

    @Test
    fun getDiagnosisTestResultRetrievalPeriodicWorkTimeInterval() {
        Assert.assertEquals(
            BackgroundWorkHelper.getDiagnosisTestResultRetrievalPeriodicWorkTimeInterval(),
            120
        )
    }

    @Test
    fun getDiagnosisKeyRetrievalMaximumCalls() {
        Assert.assertEquals(BackgroundWorkHelper.getDiagnosisKeyRetrievalMaximumCalls(), 12)
    }

    @Test
    fun getConstraintsForDiagnosisKeyOneTimeBackgroundWork() {
        val constraints = BackgroundWorkHelper.getConstraintsForDiagnosisKeyOneTimeBackgroundWork()
        Assert.assertEquals(constraints.requiredNetworkType, NetworkType.CONNECTED)
    }
}
