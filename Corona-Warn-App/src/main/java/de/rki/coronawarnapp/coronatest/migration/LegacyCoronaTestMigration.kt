package de.rki.coronawarnapp.coronatest.migration

import dagger.Reusable
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import javax.inject.Inject

@Reusable
class LegacyCoronaTestMigration @Inject constructor() {
    suspend fun load(): Set<CoronaTest> {
        TODO("Not yet implemented")
    }

    suspend fun finishMigration() {
        TODO("Not yet implemented")
    }
}
