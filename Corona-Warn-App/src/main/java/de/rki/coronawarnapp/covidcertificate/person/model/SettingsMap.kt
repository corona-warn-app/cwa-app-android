package de.rki.coronawarnapp.covidcertificate.person.model

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.KeyDeserializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.module.kotlin.readValue
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.person.model.PersonSettings
import de.rki.coronawarnapp.util.serialization.SerializationModule
import java.io.IOException

class PersonIdentifierSerializer : JsonSerializer<CertificatePersonIdentifier>() {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(
        value: CertificatePersonIdentifier?,
        gen: JsonGenerator?,
        serializers: SerializerProvider?
    ) {
        gen?.let { jGen ->
            value?.let { id ->
                jGen.writeFieldName(SerializationModule.jacksonBaseMapper.writeValueAsString(id))
            } ?: jGen.writeNull()
        }
    }
}

class PersonIdentifierDeserializer : KeyDeserializer() {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserializeKey(key: String?, ctxt: DeserializationContext?): CertificatePersonIdentifier? {
        return key?.let { SerializationModule.jacksonBaseMapper.readValue<CertificatePersonIdentifier>(key) }
    }
}

data class SettingsMap(
    @JsonSerialize(keyUsing = PersonIdentifierSerializer::class)
    @JsonDeserialize(keyUsing = PersonIdentifierDeserializer::class)
    val settings: Map<CertificatePersonIdentifier, PersonSettings>
)
