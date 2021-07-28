package de.rki.coronawarnapp.covidcertificate.common.certificate

import dagger.Reusable
import de.rki.coronawarnapp.util.serialization.validation.JsonSchemaValidator
import javax.inject.Inject

@Reusable
class DccJsonSchemaValidator @Inject constructor(
    private val dccJsonSchema: DccJsonSchema,
    private val schemaValidator: JsonSchemaValidator,
) {

    fun isValid(rawJson: String): JsonSchemaValidator.Result = schemaValidator.validate(dccJsonSchema, rawJson)
}
