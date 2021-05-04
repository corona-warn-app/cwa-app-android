package de.rki.coronawarnapp.vaccination.core.repository

import android.content.Context
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.vaccination.core.repository.storage.VaccinationStorage
import de.rki.coronawarnapp.vaccination.core.server.VaccinationProofServer
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.preferences.MockSharedPreferences

class VaccinationRepositoryTest : BaseTest() {

    @MockK lateinit var context: Context
    private lateinit var mockPreferences: MockSharedPreferences
    @MockK lateinit var timeStamper: TimeStamper

    @MockK lateinit var storage: VaccinationStorage
    @MockK lateinit var valueSetsRepository: ValueSetsRepository
    @MockK lateinit var vaccinationProofServer: VaccinationProofServer

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockPreferences = MockSharedPreferences()

        every {
            context.getSharedPreferences("vaccination_localdata", Context.MODE_PRIVATE)
        } returns mockPreferences
    }

    private fun createInstance(scope: CoroutineScope) = VaccinationRepository(
        appScope = scope,
        dispatcherProvider = TestDispatcherProvider(),
        timeStamper = timeStamper,
        storage = storage,
        valueSetsRepository = valueSetsRepository,
        vaccinationProofServer = vaccinationProofServer,
    )

    @Test
    fun `add new certificate - no prior data`() {
        TODO()
    }

    @Test
    fun `add new certificate - existing data`() {
        TODO()
    }

    @Test
    fun `add new certificate - does not match existing person`() {
        TODO()
    }

    @Test
    fun `add new certificate - duplicate certificate`() {
        TODO()
    }

    @Test
    fun `clear data`() {
        TODO()
    }

    @Test
    fun `remove certificate`() {
        TODO()
    }

    @Test
    fun `check for new proof certificate`() {
        TODO()
    }
}
