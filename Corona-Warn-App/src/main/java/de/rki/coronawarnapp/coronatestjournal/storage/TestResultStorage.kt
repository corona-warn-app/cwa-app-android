package de.rki.coronawarnapp.coronatestjournal.storage

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestGUID
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import org.joda.time.DateTime
import javax.inject.Inject

class TestResultStorage @Inject constructor(
    private val riskResultDatabaseFactory: TestResultDatabase.Factory,
) {

    private val database by lazy { riskResultDatabaseFactory.create() }
    private val testResultTable by lazy { database.testResultDao() }

    suspend fun updateResults(tests: Map<CoronaTestGUID, CoronaTest>) {

        tests.forEach { test ->
            if (test.value.isViewed && (test.value.isPositive || test.value.isNegative)){

                val result = TestResultEntity(
                    id = test.key,
                    testType = if (test.value.type == CoronaTest.Type.PCR) TestType.PCR else TestType.ANTIGEN,
                    result = if (test.value.isPositive) TestResult.POSITIVE else TestResult.NEGATIVE,
                    time = DateTime().toInstant().millis
                )
                testResultTable.insertEntry(result)
            }
        }
    }

    fun getTests() = testResultTable.allEntries()
}
