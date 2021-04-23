package de.rki.coronawarnapp.worker

import androidx.work.NetworkType
import de.rki.coronawarnapp.coronatest.execution.TestResultScheduler
import de.rki.coronawarnapp.coronatest.worker.PCRTestResultRetrievalWorker
import org.junit.Assert
import org.junit.Test

class BackgroundWorkHelperTest {

    @Test
    fun getDiagnosisTestResultRetrievalPeriodicWorkTimeInterval() {
        Assert.assertEquals(
            TestResultScheduler.getPcrTestResultRetrievalPeriodicWorkTimeInterval(),
            120
        )
    }

    @Test
    fun getConstraintsForDiagnosisKeyOneTimeBackgroundWork() {
        val constraints = BackgroundWorkHelper.getConstraintsForDiagnosisKeyOneTimeBackgroundWork()
        Assert.assertEquals(constraints.requiredNetworkType, NetworkType.CONNECTED)
    }
}
