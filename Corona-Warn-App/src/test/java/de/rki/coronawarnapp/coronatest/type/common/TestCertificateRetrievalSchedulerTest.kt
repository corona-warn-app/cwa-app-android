package de.rki.coronawarnapp.coronatest.type.common

import androidx.work.WorkManager
import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.TestCertificateRepository
import de.rki.coronawarnapp.util.device.ForegroundState
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class TestCertificateRetrievalSchedulerTest : BaseTest() {

    @MockK lateinit var workManager: WorkManager
    @MockK lateinit var certificateRepo: TestCertificateRepository
    @MockK lateinit var testRepo: CoronaTestRepository
    @MockK lateinit var foregroundState: ForegroundState

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { workManager.enqueueUniquePeriodicWork(any(), any(), any()) } returns mockk()
    }

    private fun createInstance() = TestCertificateRetrievalScheduler(
        appScope = TestCoroutineScope(),
        workManager = workManager,
        certificateRepo = certificateRepo,
        foregroundState = foregroundState,
        testRepo = testRepo,
    )

    @Test
    fun `new negative corona tests create a dcc if supported and consented`() {
        TODO()
    }

    @Test
    fun `certificates only for negative results`() {
        TODO()
    }

    @Test
    fun `no duplicate certificates for flaky test results`() {
        TODO()
    }

    @Test
    fun `refresh on foreground`() {
        TODO()
    }

    @Test
    fun `refresh on new certificate entry`() {
        TODO()
    }
}
