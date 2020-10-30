package de.rki.coronawarnapp.deadman

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkerParameters
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DeadmanNotificationSenderTest : BaseTest() {

    @MockK lateinit var context: Context
    @MockK lateinit var workerParams: WorkerParameters

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
//        every { context.get }
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createSender() = DeadmanNotificationSender(
        context = context
    )

    // TODO: Implement tests
    @Test
    fun `test one`()  {
        createSender()
    }
}
