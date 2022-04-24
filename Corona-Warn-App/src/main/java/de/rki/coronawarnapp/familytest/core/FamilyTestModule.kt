package de.rki.coronawarnapp.familytest.core

import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.familytest.core.storage.FamilyCoronaTestDao
import de.rki.coronawarnapp.familytest.core.storage.FamilyTestDatabase
import javax.inject.Singleton

@Module
class FamilyTestModule {
    @Singleton
    @Provides
    fun familyCoronaTestDao(
        factory: FamilyTestDatabase.Factory
    ): FamilyCoronaTestDao = factory.create().familyCoronaTestDao()
}
