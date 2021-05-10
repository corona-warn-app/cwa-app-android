package de.rki.coronawarnapp.vaccination.core.execution.task

import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.joda.time.Instant
import org.junit.Before
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class VaccinationUpdateTaskTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var vaccinationRepository: VaccinationRepository

    private val currentInstant = Instant.ofEpochSecond(1611764225)

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { timeStamper.nowUTC } returns currentInstant
    }

    private fun createInstance() = VaccinationUpdateTask(
        timeStamper = timeStamper,
        vaccinationRepository = vaccinationRepository
    )

    @Test
    fun `to do`() {
        TODO()
    }
}
