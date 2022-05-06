package de.rki.coronawarnapp.contactdiary.storage

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.contactdiary.storage.repo.DefaultContactDiaryRepository
import de.rki.coronawarnapp.util.reset.Resettable

@Module(includes = [ContactDiaryStorageModule.ResetModule::class])
interface ContactDiaryStorageModule {

    @Binds
    fun contactDiaryRepo(defaultContactDiaryRepository: DefaultContactDiaryRepository): ContactDiaryRepository

    @Module
    interface ResetModule {

        @Binds
        @IntoSet
        fun bindResettableContactDiaryPreferences(resettable: ContactDiaryPreferences): Resettable

        @Binds
        @IntoSet
        fun bindResettableContactDiaryRepository(resettable: DefaultContactDiaryRepository): Resettable
    }
}
