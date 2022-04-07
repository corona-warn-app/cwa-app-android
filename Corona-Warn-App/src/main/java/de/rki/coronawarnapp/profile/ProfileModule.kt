package de.rki.coronawarnapp.profile

import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.profile.storage.ProfileDao
import de.rki.coronawarnapp.profile.storage.ProfileDatabase
import javax.inject.Singleton

@Module
class ProfileModule {
    @Singleton
    @Provides
    fun familyCoronaTestDao(
        factory: ProfileDatabase.Factory
    ): ProfileDao = factory.create().profileDao()
}
