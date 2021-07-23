package de.rki.coronawarnapp.covidcertificate.expiration

import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificateRepository
import de.rki.coronawarnapp.covidcertificate.vaccination.core.CovidCertificateSettings
import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.util.TimeStamper
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DccExpirationServiceTest : BaseTest() {
    @MockK lateinit var dscCheckNotification: DccExpirationNotification
    @MockK lateinit var vaccinationRepository: VaccinationRepository
    @MockK lateinit var recoveryRepository: RecoveryCertificateRepository
    @MockK lateinit var covidCertificateSettings: CovidCertificateSettings
    @MockK lateinit var timeStamper: TimeStamper

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns Instant.EPOCH
    }

    fun createInstance() = DccExpirationNotificationService(
        dscCheckNotification = dscCheckNotification,
        vaccinationRepository = vaccinationRepository,
        recoveryRepository = recoveryRepository,
        covidCertificateSettings = covidCertificateSettings,
        timeStamper = timeStamper,
    )

    @Test
    fun `no certificates at all`() {
        TODO()
    }

    @Test
    fun `certificates that are all valid`() {
        TODO()
    }

    @Test
    fun `two expired certificates`() {
        TODO()
    }

    @Test
    fun `two soon expiring certificates`() {
        TODO()
    }

    @Test
    fun `two soon and two expired certificates`() {
        TODO()
    }

    @Test
    fun `one of each`() {
        TODO()
    }
}
