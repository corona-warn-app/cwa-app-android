package de.rki.coronawarnapp.covidcertificate.common.certificate

import android.content.res.AssetManager
import dagger.Reusable
import de.rki.coronawarnapp.util.serialization.validation.JsonSchemaSource
import okio.buffer
import okio.source
import javax.inject.Inject

@Reusable
class DccJsonSchema @Inject constructor(
    private val assets: AssetManager,
) : JsonSchemaSource {

    private val assetCache by lazy {
        assets.open("jsonschema-dcc-8b5f5ee.json").source().buffer().readUtf8()
    }
    override val rawSchema: String get() = assetCache

    override val version: JsonSchemaSource.Version = JsonSchemaSource.Version.V2019_09
}
