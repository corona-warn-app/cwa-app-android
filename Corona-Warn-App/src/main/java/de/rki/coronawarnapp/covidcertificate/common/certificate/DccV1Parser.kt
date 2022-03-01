package de.rki.coronawarnapp.covidcertificate.common.certificate

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.upokecenter.cbor.CBORObject
import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import org.joda.time.LocalDate
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DccV1Parser @Inject constructor(
    @BaseGson private val gson: Gson,
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
        val correctedJson = gson.fromJson(originalJson, JsonObject::class.java).filterExceptions().toString()
        correctedJson to gson.fromJson(correctedJson)
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
                    copy(vaccinations = listOfNotNull(vaccinations.maxByOrNull { it.vaccinatedOn ?: LocalDate(0L) }))
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

    private fun JsonElement.filterExceptions(): JsonElement =
        when (this) {
            is JsonObject -> {
                entrySet().fold(JsonObject()) { acc, (key, jsonElement) ->
                    when (jsonElement) {
                        is JsonNull -> acc
                        else -> acc.apply { add(key, jsonElement.filterExceptions()) }
                    }
                }
            }
            is JsonArray -> {
                fold(JsonArray()) { acc, jsonElement ->
                    when (jsonElement) {
                        is JsonNull -> acc
                        else -> acc.apply { add(jsonElement.filterExceptions()) }
                    }
                }
            }
            is JsonPrimitive -> if (isString) JsonPrimitive(asString?.trim()) else this

            else -> this // Should never be reached
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
