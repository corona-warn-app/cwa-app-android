package de.rki.coronawarnapp.ccl.configuration.update

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CCLConfigurationUpdateWorkerTest : BaseTest() {

    @MockK lateinit var context: Context
    @RelaxedMockK lateinit var workerParams: WorkerParameters
    @MockK lateinit var cclConfigurationUpdater: CCLConfigurationUpdater

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { cclConfigurationUpdater.updateIfRequired() } returns true
    }

    @Test
    fun `configuration updater should be called in doWork()`() = runBlockingTest {
        createWorker().doWork() shouldBe ListenableWorker.Result.success()

        coVerify(exactly = 1) { cclConfigurationUpdater.updateIfRequired() }
    }

    private fun createWorker(): CCLConfigurationUpdateWorker {
        return CCLConfigurationUpdateWorker(context, workerParams, cclConfigurationUpdater)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }
}
