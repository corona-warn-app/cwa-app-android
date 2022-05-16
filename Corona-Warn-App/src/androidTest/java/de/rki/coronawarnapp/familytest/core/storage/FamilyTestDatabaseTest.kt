package de.rki.coronawarnapp.familytest.core.storage

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.familytest.core.model.CoronaTest
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.familytest.core.model.moveToRecycleBin
import de.rki.coronawarnapp.familytest.core.model.restore
import de.rki.coronawarnapp.familytest.core.model.updateTestResult
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseTestInstrumentation

@RunWith(AndroidJUnit4::class)
class FamilyTestDatabaseTest : BaseTestInstrumentation() {

    private val database: FamilyTestDatabase =
        Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FamilyTestDatabase::class.java
        ).build()

    private val dao = database.familyCoronaTestDao()

    private val identifier = "familyTest1"

    private val test = FamilyCoronaTest(
        personName = "Maria",
        coronaTest = CoronaTest(
            identifier = identifier,
            type = BaseCoronaTest.Type.PCR,
            registeredAt = Instant.parse("2022-03-20T06:00:00.000Z"),
            registrationToken = "registrationToken1"
        )
    )

    private val identifier2 = "familyTest2"

    private val test2 = FamilyCoronaTest(
        personName = "Maria",
        coronaTest = CoronaTest(
            identifier = identifier2,
            type = BaseCoronaTest.Type.RAPID_ANTIGEN,
            registeredAt = Instant.parse("2022-03-24T11:00:00.000Z"),
            registrationToken = "registrationToken1",
            uiState = CoronaTest.UiState(),
            additionalInfo = CoronaTest.AdditionalInfo(
                createdAt = Instant.EPOCH,
                firstName = "Maria",
                lastName = "Müller",
                dateOfBirth = LocalDate.now(),
                sampleCollectedAt = Instant.EPOCH
            )
        )
    )

    private val now = Instant.parse("2022-03-30T12:00:00.000Z")

    @After
    fun teardown() {
        database.clearAllTables()
    }

    @Test
    fun testInsertAndDelete() = runTest {
        val entity = test.toEntity()
        dao.insert(entity)

        val entries = dao.getAllActive().first()
        entries.size shouldBe 1
        entries[0]!!.fromEntity() shouldBe test

        dao.update(identifier) {
            it.moveToRecycleBin(now)
        }

        dao.deleteAll()
        dao.getAllActive().first().size shouldBe 0
        dao.getAllInRecycleBin().first().size shouldBe 0
    }

    @Test
    fun testListUpdate() = runTest {
        val entity = test.toEntity()
        dao.insert(entity)
        dao.insert(test2.toEntity())

        // 2 updates on the same entity
        val updates: List<Pair<TestIdentifier, (FamilyCoronaTest) -> FamilyCoronaTest>> = listOf(
            Pair(identifier) {
                it.updateTestResult(CoronaTestResult.PCR_NEGATIVE)
            },
            Pair(identifier) {
                it.moveToRecycleBin(now)
            },
            Pair(identifier2) {
                it.updateTestResult(CoronaTestResult.RAT_REDEEMED)
            }
        )

        dao.update(updates)

        dao.getAllActive().first().size shouldBe 1
        val entries = dao.getAllInRecycleBin().first()
        entries.size shouldBe 1
        entries[0]!!.test.coronaTest.state shouldBe CoronaTest.State.NEGATIVE
    }

    @Test
    fun testMoveToRecycleBin() = runTest {
        val entity = test.toEntity()
        dao.insert(entity)
        dao.insert(test2.toEntity())

        dao.moveAllToRecycleBin(listOf(identifier, identifier2), now.millis)

        dao.getAllActive().first().size shouldBe 0
        val entries = dao.getAllInRecycleBin().first()
        entries.size shouldBe 2
        entries.forEach { it!!.movedToRecycleBinAtMillis shouldBe now.millis }
    }

    @Test
    fun testLifeCycle() = runTest {
        val entity = test.toEntity()
        dao.insert(entity)

        val entries = dao.getAllActive().first()
        entries.size shouldBe 1
        entries[0]!!.fromEntity() shouldBe test

        dao.update(identifier) {
            it.updateTestResult(CoronaTestResult.PCR_NEGATIVE)
        }
        dao.getAllActive().first()[0]!!.test.testResult shouldBe CoronaTestResult.PCR_NEGATIVE

        dao.update(identifier) {
            it.moveToRecycleBin(now)
        }

        dao.getAllActive().first().size shouldBe 0
        dao.getAllInRecycleBin().first().size shouldBe 1

        dao.update(identifier) {
            it.restore()
        }

        dao.getAllActive().first().size shouldBe 1
        dao.getAllInRecycleBin().first().size shouldBe 0

        dao.deleteAll()

        dao.getAllActive().first().size shouldBe 0
        dao.getAllInRecycleBin().first().size shouldBe 0
    }
}
