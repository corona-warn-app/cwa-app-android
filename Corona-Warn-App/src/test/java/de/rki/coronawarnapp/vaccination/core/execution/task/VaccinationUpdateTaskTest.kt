package de.rki.coronawarnapp.vaccination.core.execution.task

import de.rki.coronawarnapp.vaccination.core.repository.VaccinationRepository
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class VaccinationUpdateTaskTest : BaseTest() {

    @MockK lateinit var vaccinationRepository: VaccinationRepository

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { vaccinationRepository.refresh(any()) } just Runs
    }

    private fun createInstance() = VaccinationUpdateTask(
        vaccinationRepository = vaccinationRepository
    )

    @Test
    fun `task calls generic refresh`() = runBlockingTest {
        val task = createInstance()

        task.run(VaccinationUpdateTask.Arguments)

        coVerify { vaccinationRepository.refresh(null) }
    }
}
