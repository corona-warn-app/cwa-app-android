package de.rki.coronawarnapp.familytest.core.storage

import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.joda.time.Instant
import javax.inject.Inject

class FamilyTestStorage @Inject constructor(
    private val dao: FamilyCoronaTestDao
) {

    val familyTestMap: Flow<Map<TestIdentifier, FamilyCoronaTest>> = dao.getAllActive().map { list ->
        list.filterNotNull().associate { it.identifier to it.fromEntity() }
    }

    val familyTestRecycleBinMap: Flow<Map<TestIdentifier, FamilyCoronaTest>> = dao.getAllInRecycleBin().map { list ->
        list.filterNotNull().associate { it.identifier to it.fromEntity() }
    }

    suspend fun save(test: FamilyCoronaTest) {
        dao.insert(test.toEntity())
    }

    suspend fun update(identifier: TestIdentifier, update: suspend (FamilyCoronaTest) -> FamilyCoronaTest) {
        dao.update(identifier, update)
    }

    suspend fun updateAll(identifiers: Set<TestIdentifier>, update: suspend (FamilyCoronaTest) -> FamilyCoronaTest) {
        dao.updateAll(identifiers, update)
    }

    suspend fun delete(test: FamilyCoronaTest) {
        dao.delete(test.toEntity())
    }

    suspend fun clear() {
        dao.deleteAll()
    }
}

fun FamilyCoronaTest.toEntity(): FamilyCoronaTestEntity {
    return FamilyCoronaTestEntity(
        identifier = coronaTest.identifier,
        movedToRecycleBinAtMillis = coronaTest.recycledAt?.millis,
        test = this,
    )
}

fun FamilyCoronaTestEntity.fromEntity(): FamilyCoronaTest {
    val recycledAt = movedToRecycleBinAtMillis?.let { Instant.ofEpochMilli(it) }
    return test.copy(
        coronaTest = test.coronaTest.copy(recycledAt = recycledAt)
    )
}
