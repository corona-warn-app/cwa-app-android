package de.rki.coronawarnapp.contactdiary.storage.settings

import com.fasterxml.jackson.databind.ObjectMapper
import de.rki.coronawarnapp.util.datastore.BaseJsonSerializer
import de.rki.coronawarnapp.util.serialization.BaseJackson
import javax.inject.Inject

class ContactDiarySettingsSerializer @Inject constructor(
    @BaseJackson objectMapper: ObjectMapper
) : BaseJsonSerializer<ContactDiarySettings>(objectMapper) {

    override val defaultValue: ContactDiarySettings
        get() = ContactDiarySettings()
}
