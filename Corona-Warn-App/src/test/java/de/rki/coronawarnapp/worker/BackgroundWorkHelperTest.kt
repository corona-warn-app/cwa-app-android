package de.rki.coronawarnapp.worker

import androidx.work.NetworkType
import de.rki.coronawarnapp.coronatest.worker.execution.PCRTestResultScheduler
import org.junit.Assert
import org.junit.Test

class BackgroundWorkHelperTest {

    @Test
    fun getDiagnosisTestResultRetrievalPeriodicWorkTimeInterval() {
        Assert.assertEquals(
            PCRTestResultScheduler.getPcrTestResultRetrievalPeriodicWorkTimeInterval(),
            120
        )
    }

    @Test
    fun getConstraintsForDiagnosisKeyOneTimeBackgroundWork() {
        val constraints = BackgroundWorkHelper.getConstraintsForDiagnosisKeyOneTimeBackgroundWork()
        Assert.assertEquals(constraints.requiredNetworkType, NetworkType.CONNECTED)
    }
}
