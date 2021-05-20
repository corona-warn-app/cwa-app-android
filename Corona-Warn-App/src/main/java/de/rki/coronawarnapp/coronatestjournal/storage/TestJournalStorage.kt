package de.rki.coronawarnapp.coronatestjournal.storage

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestGUID
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import javax.inject.Inject

class TestJournalStorage @Inject constructor(
    private val riskJournalDatabaseFactory: TestJournalDatabase.Factory,
) {
    private val database by lazy { riskJournalDatabaseFactory.create() }
    private val testResultTable by lazy { database.testResultDao() }

    fun getTests() = testResultTable.allTests()

    suspend fun updateResults(tests: Map<CoronaTestGUID, CoronaTest>) {
        tests.filter { it.value.canBeAddedToJournal() }
            .map { it.asTestResultEntity() }
            .forEach { testResultTable.insertTest(it) }
    }

    suspend fun deleteAll() = testResultTable.deleteAll()

    private fun CoronaTest.canBeAddedToJournal(): Boolean {
        return isViewed && (isNegative || isPositive)
    }

    private fun Map.Entry<CoronaTestGUID, CoronaTest>.asTestResultEntity(): TestJournalEntity {
        return with(value) {
            TestJournalEntity(
                id = key,
                testType = if (type == CoronaTest.Type.PCR)
                    TestJournalEntity.TestType.PCR
                else
                    TestJournalEntity.TestType.ANTIGEN,
                result = if (isPositive)
                    TestJournalEntity.TestResult.POSITIVE
                else
                    TestJournalEntity.TestResult.NEGATIVE,
                time = when (this) {
                    is RACoronaTest -> testedAt
                    else -> registeredAt
                }
            )
        }
    }
}
