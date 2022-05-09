package de.rki.coronawarnapp.familytest.core.storage

import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FamilyTestStorage @Inject constructor(
    private val dao: FamilyCoronaTestDao
) : Resettable {

    val familyTestMap: Flow<Map<TestIdentifier, FamilyCoronaTest>> = dao.getAllActive().map { list ->
        list.filterNotNull().associate { it.identifier to it.fromEntity() }
    }

    val familyTestRecycleBinMap: Flow<Map<TestIdentifier, FamilyCoronaTest>> = dao.getAllInRecycleBin().map { list ->
        list.filterNotNull().associate { it.identifier to it.fromEntity() }
    }

    suspend fun save(test: FamilyCoronaTest) {
        dao.insert(test.toEntity())
    }

    suspend fun update(identifier: TestIdentifier, update: (FamilyCoronaTest) -> FamilyCoronaTest) {
        dao.update(identifier, update)
    }

    suspend fun update(updates: List<Pair<TestIdentifier, (FamilyCoronaTest) -> FamilyCoronaTest>>) {
        dao.update(updates)
    }

    suspend fun delete(test: FamilyCoronaTest) {
        dao.delete(test.toEntity())
    }

    suspend fun moveAllToRecycleBin(identifiers: List<TestIdentifier>, atInstant: Instant) {
        dao.moveAllToRecycleBin(identifiers, atInstant.millis)
    }

    override suspend fun reset() {
        Timber.d("reset()")
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
