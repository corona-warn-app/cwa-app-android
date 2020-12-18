package de.rki.coronawarnapp.contactdiary.storage

import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.contactdiary.storage.repo.DefaultContactDiaryRepository
import javax.inject.Singleton

@Module
class ContactDiaryStorageModule {

    @Singleton
    @Provides
    fun contactDiaryRepo(defaultContactDiaryRepository: DefaultContactDiaryRepository): ContactDiaryRepository =
        defaultContactDiaryRepository
}
