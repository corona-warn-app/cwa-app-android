package de.rki.coronawarnapp.worker

import androidx.work.NetworkType
import org.junit.Assert
import org.junit.Test

class BackgroundWorkHelperTest {

    @Test
    fun getDiagnosisTestResultRetrievalPeriodicWorkTimeInterval() {
        Assert.assertEquals(
            BackgroundWorkHelper.getDiagnosisTestResultRetrievalPeriodicWorkTimeInterval(),
            120
        )
    }

    @Test
    fun getConstraintsForDiagnosisKeyOneTimeBackgroundWork() {
        val constraints = BackgroundWorkHelper.getConstraintsForDiagnosisKeyOneTimeBackgroundWork()
        Assert.assertEquals(constraints.requiredNetworkType, NetworkType.CONNECTED)
    }
}
