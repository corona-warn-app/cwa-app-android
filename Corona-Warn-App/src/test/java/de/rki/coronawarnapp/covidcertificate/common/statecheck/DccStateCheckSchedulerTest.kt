package de.rki.coronawarnapp.covidcertificate.common.statecheck

import androidx.work.WorkManager
import de.rki.coronawarnapp.covidcertificate.expiration.DccExpirationNotificationService
import de.rki.coronawarnapp.covidcertificate.signature.core.DscRepository
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.device.ForegroundState
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DccStateCheckSchedulerTest : BaseTest() {
    @MockK lateinit var foregroundState: ForegroundState
    @MockK lateinit var workManager: WorkManager
    @MockK lateinit var dccExpirationNotificationService: DccExpirationNotificationService
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var dscRepository: DscRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    fun createInstance(scope: CoroutineScope) = DccStateCheckScheduler(
        appScope = scope,
        foregroundState = foregroundState,
        workManager = workManager,
        dccExpirationNotificationService = dccExpirationNotificationService,
        dscRepository = dscRepository,
        timeStamper = timeStamper
    )

    @Test
    fun `schedule expiration worker on setup`() {
        TODO()
    }

    @Test
    fun `force expiration state check when app comes into foreground`() {
        TODO()
    }

    @Test
    fun `refresh dsc data when app comes into foreground`() {
        TODO()
    }

    @Test
    fun `do not refresh dsc data when last refresh was recent`() {
        TODO()
    }
}
