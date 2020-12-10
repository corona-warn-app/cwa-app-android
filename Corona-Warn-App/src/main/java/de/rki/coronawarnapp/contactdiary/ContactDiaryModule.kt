package de.rki.coronawarnapp.contactdiary

import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.contactdiary.storage.ContactDiaryDatabase
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryDateDao
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryElementDao
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryLocationDao
import de.rki.coronawarnapp.contactdiary.storage.dao.ContactDiaryPersonDao
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.contactdiary.storage.repo.DefaultContactDiaryRepository
import javax.inject.Singleton

@Module
class ContactDiaryModule {

    @Singleton
    @Provides
    fun contactDiaryDatabase(contactDiaryDatabaseFactory: ContactDiaryDatabase.Factory): ContactDiaryDatabase =
        contactDiaryDatabaseFactory.create()

    @Singleton
    @Provides
    fun contactDiaryDateDao(contactDiaryDatabase: ContactDiaryDatabase): ContactDiaryDateDao =
        contactDiaryDatabase.contactDiaryDateDao()

    @Singleton
    @Provides
    fun contactDiaryElementDao(contactDiaryDatabase: ContactDiaryDatabase): ContactDiaryElementDao =
        contactDiaryDatabase.contactDiaryElementDao()

    @Singleton
    @Provides
    fun locationDao(contactDiaryDatabase: ContactDiaryDatabase): ContactDiaryLocationDao =
        contactDiaryDatabase.locationDao()

    @Singleton
    @Provides
    fun personDao(contactDiaryDatabase: ContactDiaryDatabase): ContactDiaryPersonDao =
        contactDiaryDatabase.personDao()

    @Singleton
    @Provides
    fun contactDiaryRepo(defaultContactDiaryRepository: DefaultContactDiaryRepository): ContactDiaryRepository = defaultContactDiaryRepository
}
