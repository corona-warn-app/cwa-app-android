package de.rki.coronawarnapp.contactdiary

import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.contactdiary.storage.ContactDiaryDatabase
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryLocationDao
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryLocationVisitDao
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryPersonDao
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryPersonEncounterDao
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.contactdiary.storage.repo.DefaultContactDiaryRepository
import javax.inject.Singleton

@Module
class ContactDiaryModule {

    @Singleton
    @Provides
    fun contactDiaryDatabase(contactDiaryDatabaseFactory: ContactDiaryDatabase.Factory): ContactDiaryDatabase =
        contactDiaryDatabaseFactory.create()

    @Provides
    fun locationDao(contactDiaryDatabase: ContactDiaryDatabase): ContactDiaryLocationDao =
        contactDiaryDatabase.locationDao()

    @Provides
    fun locationVisitDao(contactDiaryDatabase: ContactDiaryDatabase): ContactDiaryLocationVisitDao =
        contactDiaryDatabase.locationVisitDao()

    @Provides
    fun personDao(contactDiaryDatabase: ContactDiaryDatabase): ContactDiaryPersonDao =
        contactDiaryDatabase.personDao()

    @Provides
    fun personEncounterDao(contactDiaryDatabase: ContactDiaryDatabase): ContactDiaryPersonEncounterDao =
        contactDiaryDatabase.personEncounterDao()

    @Singleton
    @Provides
    fun contactDiaryRepo(defaultContactDiaryRepository: DefaultContactDiaryRepository): ContactDiaryRepository =
        defaultContactDiaryRepository
}
