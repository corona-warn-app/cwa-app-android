package de.rki.coronawarnapp.util.serialization.validation

import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import com.networknt.schema.ValidationMessage
import dagger.Reusable
import javax.inject.Inject

@Reusable
class JsonSchemaValidator @Inject constructor() {

    fun validate(
        schemaSource: JsonSchemaSource,
        rawJson: String,
    ): Result {
        val schema = when (schemaSource.version) {
            JsonSchemaSource.Version.V2019_19 -> {
                val factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909)
                factory.getSchema(schemaSource.rawSchema)
            }
        }

        val json = ObjectMapper().readTree(rawJson)

        val errors = schema.validate(json)

        return Result(errors = errors)
    }

    data class Result(
        internal val errors: Set<ValidationMessage>,
    ) {
        val isValid: Boolean get() = errors.isEmpty()

        val invalidityReason: String? = errors.firstOrNull()?.message
    }
}
