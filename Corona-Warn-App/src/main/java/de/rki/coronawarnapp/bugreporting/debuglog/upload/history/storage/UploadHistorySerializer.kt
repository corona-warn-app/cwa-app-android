package de.rki.coronawarnapp.bugreporting.debuglog.upload.history.storage

import com.fasterxml.jackson.databind.ObjectMapper
import de.rki.coronawarnapp.bugreporting.debuglog.upload.history.model.UploadHistory
import de.rki.coronawarnapp.util.datastore.BaseJsonSerializer
import de.rki.coronawarnapp.util.serialization.BaseJackson
import javax.inject.Inject

class UploadHistorySerializer @Inject constructor(
    @BaseJackson objectMapper: ObjectMapper
) : BaseJsonSerializer<UploadHistory>(objectMapper) {

    override val defaultValue: UploadHistory
        get() = UploadHistory()
}
