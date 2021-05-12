package de.rki.coronawarnapp.vaccination.core.execution.task

import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class VaccinationUpdateTaskTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var vaccinationRepository: VaccinationRepository

    private val currentInstant = Instant.ofEpochSecond(1611764225)

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns currentInstant

        coEvery { vaccinationRepository.refresh(any()) } just Runs
    }

    private fun createInstance() = VaccinationUpdateTask(
        timeStamper = timeStamper,
        vaccinationRepository = vaccinationRepository
    )

    @Test
    fun `task calls generic refresh`() = runBlockingTest {
        val task = createInstance()

        task.run(VaccinationUpdateTask.Arguments)

        coVerify { vaccinationRepository.refresh(null) }
    }
}
