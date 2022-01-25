package de.rki.coronawarnapp.ccl.configuration.validation

import dagger.Reusable
import de.rki.coronawarnapp.util.serialization.validation.JsonSchemaValidator
import javax.inject.Inject

@Reusable
class CCLConfigurationJsonSchemaValidator @Inject constructor(
    private val cclConfigurationJsonSchema: CCLConfigurationJsonSchema,
    private val schemaValidator: JsonSchemaValidator,
) {
    fun isValid(rawJson: String): JsonSchemaValidator.Result =
        schemaValidator.validate(cclConfigurationJsonSchema, rawJson)
}
