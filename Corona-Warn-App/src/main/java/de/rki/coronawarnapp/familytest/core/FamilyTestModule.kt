package de.rki.coronawarnapp.familytest.core

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.familytest.core.storage.FamilyCoronaTestDao
import de.rki.coronawarnapp.familytest.core.storage.FamilyTestDatabase
import de.rki.coronawarnapp.familytest.core.storage.FamilyTestStorage
import de.rki.coronawarnapp.util.reset.Resettable
import javax.inject.Singleton

@Module(includes = [FamilyTestModule.BindsModule::class])
object FamilyTestModule {
    @Singleton
    @Provides
    fun familyCoronaTestDao(
        factory: FamilyTestDatabase.Factory
    ): FamilyCoronaTestDao = factory.create().familyCoronaTestDao()

    @Module
    internal interface BindsModule {

        @Binds
        @IntoSet
        fun bindResettableFamilyTestStorage(resettable: FamilyTestStorage): Resettable
    }
}
