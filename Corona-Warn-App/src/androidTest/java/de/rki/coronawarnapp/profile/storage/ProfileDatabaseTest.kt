package de.rki.coronawarnapp.profile.storage

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.rki.coronawarnapp.profile.model.Profile
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseTestInstrumentation

@RunWith(AndroidJUnit4::class)
class ProfileDatabaseTest : BaseTestInstrumentation() {

    private val database: ProfileDatabase =
        Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ProfileDatabase::class.java
        ).build()

    private val dao = database.profileDao()

    private val entity = Profile(
        firstName = "Anna"
    ).toEntity()

    private val entity2 = Profile(
        firstName = "David"
    ).toEntity()

    @After
    fun teardown() {
        database.clearAllTables()
    }

    @Test
    fun testInsertUpdateDelete() = runBlocking {
        dao.insert(entity)
        dao.insert(entity2)
        val entries = dao.getAll().first()
        entries.size shouldBe 2
        val anna = entries.find { it.firstName == "Anna" }
        dao.update(anna!!.copy(lastName = "Meier"))
        dao.getAll().first().find { it.lastName == "Meier"}!!.firstName shouldBe "Anna"
        dao.deleteAll()
        dao.getAll().first() shouldBe emptyList()
    }
}
