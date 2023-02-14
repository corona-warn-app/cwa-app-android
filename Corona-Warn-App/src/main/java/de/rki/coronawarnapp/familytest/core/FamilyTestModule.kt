package de.rki.coronawarnapp.familytest.core

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.familytest.core.storage.FamilyCoronaTestDao
import de.rki.coronawarnapp.familytest.core.storage.FamilyTestDatabase
import de.rki.coronawarnapp.familytest.core.storage.FamilyTestStorage
import de.rki.coronawarnapp.util.reset.Resettable
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object FamilyTestModule {
    @Singleton
    @Provides
    fun familyCoronaTestDao(
        factory: FamilyTestDatabase.Factory
    ): FamilyCoronaTestDao = factory.create().familyCoronaTestDao()

    @InstallIn(SingletonComponent::class)
    @Module
    internal interface ResetModule {

        @Binds
        @IntoSet
        fun bindResettableFamilyTestStorage(resettable: FamilyTestStorage): Resettable
    }
}
