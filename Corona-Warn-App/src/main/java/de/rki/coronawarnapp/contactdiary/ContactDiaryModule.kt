package de.rki.coronawarnapp.contactdiary

import dagger.Module
import de.rki.coronawarnapp.contactdiary.storage.ContactDiaryStorageModule

@Module(
    includes = [ContactDiaryStorageModule::class]
)
class ContactDiaryModule
