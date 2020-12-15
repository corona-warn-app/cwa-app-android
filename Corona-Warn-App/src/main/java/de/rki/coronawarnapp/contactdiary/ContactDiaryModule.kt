package de.rki.coronawarnapp.contactdiary

import dagger.Module
import de.rki.coronawarnapp.contactdiary.storage.ContactDiaryStorageModule
import de.rki.coronawarnapp.contactdiary.ui.edit.ContactDiaryEditModule

@Module
    (
    includes = [
        ContactDiaryStorageModule::class,
        ContactDiaryEditModule::class
    ]
)
class ContactDiaryModule
