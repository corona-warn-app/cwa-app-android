package de.rki.coronawarnapp.contactdiary

import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.contactdiary.retention.ContactDiaryRetentionModule
import de.rki.coronawarnapp.contactdiary.storage.ContactDiaryStorageModule
import de.rki.coronawarnapp.contactdiary.ui.ContactDiaryActivity
import de.rki.coronawarnapp.contactdiary.ui.ContactDiaryUIModule

@Module(
    includes = [
        ContactDiaryStorageModule::class,
        ContactDiaryRetentionModule::class
    ]
)
abstract class ContactDiaryRootModule {
    @ContributesAndroidInjector(modules = [ContactDiaryUIModule::class])
    abstract fun contactDiaryActivity(): ContactDiaryActivity
}
