package de.rki.coronawarnapp.ccl.configuration.validation

import android.content.res.AssetManager
import de.rki.coronawarnapp.util.serialization.validation.JsonSchemaSource
import javax.inject.Inject

class CCLConfigurationJsonSchema @Inject constructor(
    private val assetManager: AssetManager
) : JsonSchemaSource {
    override val rawSchema: String
        get() = TODO("Not yet implemented")
    override val version: JsonSchemaSource.Version
        get() = TODO("Not yet implemented")
}
