package de.rki.coronawarnapp.worker

import de.rki.coronawarnapp.coronatest.type.pcr.execution.PCRResultScheduler
import org.junit.Assert
import org.junit.Test

class BackgroundWorkHelperTest {

    @Test
    fun getDiagnosisTestResultRetrievalPeriodicWorkTimeInterval() {
        Assert.assertEquals(
            PCRResultScheduler.getPcrTestResultRetrievalPeriodicWorkTimeInterval(),
            120
        )
    }
}
