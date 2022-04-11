package de.rki.coronawarnapp.profile.storage

import de.rki.coronawarnapp.profile.model.Profile
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ProfileRepositoryTest {

    @MockK lateinit var dao: ProfileDao

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        coEvery { dao.getAll() } returns flowOf(emptyList())
    }

    @Test
    fun `upsert works with id`() {
        val instance = createInstance()
        val profile = Profile(id = 1)
        instance.upsertProfile(profile)
        coVerify { dao.update(profile.toEntity()) }
    }

    @Test
    fun `upsert works without id`() {
        val instance = createInstance()
        val profile = Profile(firstName = "Jo")
        instance.upsertProfile(profile)
        coVerify { dao.insert(profile.toEntity()) }
    }

    @Test
    fun `delete works`() {
        val instance = createInstance()
        instance.deleteProfile(1)
        coVerify { dao.delete(1) }
    }

    @Test
    fun `clear works`() {
        val instance = createInstance()
        instance.clear()
        coVerify { dao.deleteAll() }
    }

    fun createInstance() = ProfileRepository(
        dao,
        TestCoroutineScope()
    )
}
