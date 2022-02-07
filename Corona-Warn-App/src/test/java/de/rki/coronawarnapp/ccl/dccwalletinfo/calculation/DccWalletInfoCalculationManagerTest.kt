package de.rki.coronawarnapp.ccl.dccwalletinfo.calculation

import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import de.rki.coronawarnapp.covidcertificate.booster.BoosterNotificationService
import de.rki.coronawarnapp.covidcertificate.booster.BoosterRulesRepository
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.util.TimeStamper
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2

class DccWalletInfoCalculationManagerTest : BaseTest() {

    @MockK lateinit var boosterRulesRepository: BoosterRulesRepository
    @MockK lateinit var boosterNotificationService: BoosterNotificationService
    @MockK lateinit var personCertificatesProvider: PersonCertificatesProvider
    @MockK lateinit var dccWalletInfoRepository: DccWalletInfoRepository
    @MockK lateinit var calculation: DccWalletInfoCalculation
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var certificates: PersonCertificates

    lateinit var instance: DccWalletInfoCalculationManager

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { personCertificatesProvider.personCertificates } returns flowOf(setOf(certificates))
        every { timeStamper.nowUTC } returns Instant.EPOCH
        instance = DccWalletInfoCalculationManager(
            boosterRulesRepository,
            boosterNotificationService,
            personCertificatesProvider,
            dccWalletInfoRepository,
            calculation,
            timeStamper
        )
    }

    @Test
    fun `catches exception`() {
        every { calculation.getDccWalletInfo(any()) } throws Exception()
        assertDoesNotThrow {
            runBlockingTest2 {
                instance.triggerCalculation()
            }
        }
    }
}
