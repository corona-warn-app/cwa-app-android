package de.rki.coronawarnapp.covidcertificate.common.certificate

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.MissingNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.module.kotlin.readValue
import com.upokecenter.cbor.CBORObject
import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode
import de.rki.coronawarnapp.util.serialization.BaseJackson
import timber.log.Timber
import java.time.Instant
import java.time.ZoneOffset
import javax.inject.Inject

@Reusable
class DccV1Parser @Inject constructor(
    @BaseJackson private val mapper: ObjectMapper,
    private val dccJsonSchemaValidator: DccJsonSchemaValidator,
) {
    fun parse(map: CBORObject, mode: Mode): Body = try {
        val dgcCbor = map[keyHCert].let {
            if (it == null) throw InvalidHealthCertificateException(ErrorCode.HC_CWT_NO_HCERT)
            it[keyEuDgcV1] ?: throw InvalidHealthCertificateException(ErrorCode.HC_CWT_NO_DGC)
        }

        val (rawBody, dcc) = dgcCbor.toCertificate()
        val checkedDcc = dcc.toValidated(mode)
        rawBody.checkSchema(mode)
        Body(parsed = checkedDcc, raw = rawBody)
    } catch (e: InvalidHealthCertificateException) {
        throw e
    } catch (e: Throwable) {
        throw InvalidHealthCertificateException(ErrorCode.HC_CBOR_DECODING_FAILED, cause = e)
    }

    private fun CBORObject.toCertificate(): Pair<String, DccV1> = try {
        val originalJson = ToJSONString()
        val correctedJson = mapper.readValue(originalJson, JsonNode::class.java).filterExceptions().toString()
        correctedJson to mapper.readValue(correctedJson)
    } catch (e: InvalidHealthCertificateException) {
        throw e
    } catch (e: Throwable) {
        throw InvalidHealthCertificateException(ErrorCode.HC_JSON_SCHEMA_INVALID)
    }

    private fun DccV1.toValidated(mode: Mode): DccV1 = try {
        checkModeRestrictions(mode).apply {
            // Apply otherwise we risk accidentally accessing the original obj in the outer scope
            require(isSingleCertificate())
        }
    } catch (e: InvalidHealthCertificateException) {
        throw e
    } catch (e: Throwable) {
        throw InvalidHealthCertificateException(ErrorCode.HC_JSON_SCHEMA_INVALID)
    }

    private fun DccV1.checkModeRestrictions(mode: Mode) = when (mode) {
        Mode.CERT_VAC_STRICT ->
            if (vaccinations?.size != 1)
                throw InvalidHealthCertificateException(
                    if (vaccinations.isNullOrEmpty()) ErrorCode.NO_VACCINATION_ENTRY
                    else ErrorCode.MULTIPLE_VACCINATION_ENTRIES
                )
            else this
        Mode.CERT_VAC_LENIENT -> {
            when {
                vaccinations.isNullOrEmpty() -> throw InvalidHealthCertificateException(ErrorCode.NO_VACCINATION_ENTRY)
                vaccinations.size > 1 -> {
                    Timber.w("Lenient: Vaccination data contained multiple entries.")
                    val defaultLocalDate = Instant.ofEpochMilli(0L).atZone(ZoneOffset.UTC).toLocalDate()
                    copy(vaccinations = listOfNotNull(vaccinations.maxByOrNull { it.vaccinatedOn ?: defaultLocalDate }))
                }
                else -> this
            }
        }
        Mode.CERT_REC_STRICT ->
            if (recoveries?.size != 1)
                throw InvalidHealthCertificateException(
                    if (recoveries.isNullOrEmpty()) ErrorCode.NO_RECOVERY_ENTRY
                    else ErrorCode.MULTIPLE_RECOVERY_ENTRIES
                )
            else this
        Mode.CERT_TEST_STRICT ->
            if (tests?.size != 1)
                throw InvalidHealthCertificateException(
                    if (tests.isNullOrEmpty()) ErrorCode.NO_TEST_ENTRY
                    else ErrorCode.MULTIPLE_TEST_ENTRIES
                )
            else this
        else -> this
    }

    private fun DccV1.isSingleCertificate(): Boolean {
        return (vaccinations?.size ?: 0) + (tests?.size ?: 0) + (recoveries?.size ?: 0) == 1
    }

    private fun String.checkSchema(mode: Mode) = when (mode) {
        Mode.CERT_VAC_STRICT,
        Mode.CERT_SINGLE_STRICT,
        Mode.CERT_REC_STRICT,
        Mode.CERT_TEST_STRICT -> dccJsonSchemaValidator.isValid(this).let {
            if (it.isValid) return@let this
            throw InvalidHealthCertificateException(
                errorCode = ErrorCode.HC_JSON_SCHEMA_INVALID,
                IllegalArgumentException("Schema Validation did not pass:\n${it.invalidityReason}")
            )
        }
        Mode.CERT_REC_LENIENT,
        Mode.CERT_TEST_LENIENT,
        Mode.CERT_VAC_LENIENT -> {
            // We don't check schema in lenient mode, it may affect already stored certificates.
            this
        }
    }

    private fun JsonNode.filterExceptions(): JsonNode {
        return when (this) {
            is ObjectNode -> {
                fields().asSequence().fold(JsonNodeFactory.instance.objectNode()) { acc, (key, jsonNode) ->
                    when (jsonNode) {
                        is NullNode -> acc
                        is MissingNode -> acc
                        else -> acc.apply { set<JsonNode>(key, jsonNode.filterExceptions()) }
                    }
                }
            }
            is ArrayNode -> {
                fold(JsonNodeFactory.instance.arrayNode()) { acc, jsonNode ->
                    when (jsonNode) {
                        is NullNode -> acc
                        is MissingNode -> acc
                        else -> acc.apply { add(jsonNode.filterExceptions()) }
                    }
                }
            }
            is TextNode -> JsonNodeFactory.instance.textNode(this.asText().trim())
            else -> this
        }
    }

    enum class Mode {
        CERT_VAC_STRICT, // exactly one vaccination certificate allowed
        CERT_VAC_LENIENT, // multiple vaccination certificates allowed, no schema check
        CERT_REC_STRICT, // exactly one recovery certificate allowed
        CERT_REC_LENIENT, // exactly one recovery certificate allowed, no schema check
        CERT_TEST_STRICT, // exactly one test certificate allowed
        CERT_TEST_LENIENT, // exactly one test certificate allowed, no schema check
        CERT_SINGLE_STRICT; // exactly one certificate allowed
    }

    data class Body(
        val raw: String,
        val parsed: DccV1,
    )

    companion object {
        private val keyEuDgcV1 = CBORObject.FromObject(1)
        private val keyHCert = CBORObject.FromObject(-260)
    }
}
