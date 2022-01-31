package de.rki.coronawarnapp.covidcertificate.booster

import de.rki.coronawarnapp.covidcertificate.vaccination.core.repository.VaccinationRepository
import de.rki.coronawarnapp.util.TimeStamper
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class BoosterNotificationServiceTest : BaseTest() {

    @MockK lateinit var boosterNotificationSender: BoosterNotificationSender
    @MockK lateinit var vaccinationRepository: VaccinationRepository
    @MockK lateinit var timeStamper: TimeStamper

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { timeStamper.nowUTC } returns Instant.parse("2021-01-01T00:00:00.000Z")
        every { boosterNotificationSender.showBoosterNotification(any()) } just Runs

        coEvery { vaccinationRepository.updateBoosterNotifiedAt(any(), any()) } just Runs
        coEvery { vaccinationRepository.clearBoosterRuleInfoIfNecessary(any(), any()) } just Runs
    }

    private fun service() = BoosterNotificationService(
        boosterNotificationSender = boosterNotificationSender,
        vaccinationRepository = vaccinationRepository,
        timeStamper = timeStamper
    )

    @Test
    fun test() {
        val service = service()
        TODO()
    }
}
