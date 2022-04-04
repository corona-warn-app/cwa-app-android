package de.rki.coronawarnapp.familytest.worker

import android.content.Context
import androidx.work.WorkerParameters
import de.rki.coronawarnapp.familytest.core.repository.FamilyTestRepository
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class FamilyTestRetrievalWorkerTest : BaseTest() {

    @MockK lateinit var context: Context
    @RelaxedMockK lateinit var workerParams: WorkerParameters
    @MockK lateinit var repository: FamilyTestRepository
    @MockK lateinit var familyTestResultRetrievalScheduler: FamilyTestResultRetrievalScheduler

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery { repository.refresh() } returns emptyMap()
        coEvery { familyTestResultRetrievalScheduler.checkPollingSchedule() } just Runs
    }

    private fun createWorker() = FamilyTestResultRetrievalWorker(
        context = context,
        workerParams = workerParams,
        repository, familyTestResultRetrievalScheduler
    )

    @Test
    fun `create worker`() = runBlockingTest {
        createWorker().doWork()

        coVerifySequence {
            repository.refresh()
            familyTestResultRetrievalScheduler.checkPollingSchedule()
        }
    }
}
