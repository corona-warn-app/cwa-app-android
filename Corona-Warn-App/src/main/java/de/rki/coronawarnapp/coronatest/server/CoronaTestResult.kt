package de.rki.coronawarnapp.coronatest.server

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import org.joda.time.Instant

data class CoronaTestResultResponse(
    val coronaTestResult: CoronaTestResult,
    val sampleCollectedAt: Instant? = null, // Only used for RA tests
    val labId: String? = null, // May initially be null while test is pending
) {
    companion object {
        fun fromResponse(response: VerificationApiV1.TestResultResponse) =
            CoronaTestResultResponse(
                coronaTestResult = CoronaTestResult.fromInt(response.testResult),
                sampleCollectedAt = response.sampleCollectedAt?.toLong()?.let { Instant.ofEpochSecond(it) },
                labId = response.labId
            )
    }
}

enum class CoronaTestResult(val value: Int) {
    /**
     * Pending (PCR test) or Pending (rapid antigen test)
     */
    PCR_OR_RAT_PENDING(0),

    /**
     * Negative (PCR test)
     */
    PCR_NEGATIVE(1),

    /**
     * Positive (PCR test)
     */
    PCR_POSITIVE(2),

    /**
     * Invalid (PCR test)
     */
    PCR_INVALID(3),

    /**
     * Redeemed (PCR test; locally referred to as Expired)
     */
    PCR_OR_RAT_REDEEMED(4),

    /**
     * 	Pending (rapid antigen test)
     */
    RAT_PENDING(5),

    /**
     *  Negative (rapid antigen test)
     */
    RAT_NEGATIVE(6),

    /**
     * Positive (rapid antigen test)
     */
    RAT_POSITIVE(7),

    /**
     * 	Invalid (rapid antigen test)
     */
    RAT_INVALID(8),

    /**
     * Redeemed (rapid antigen test; locally referred to as Expired))
     */
    RAT_REDEEMED(9);

    override fun toString(): String = "$name($value)"

    companion object {
        fun fromInt(value: Int) = values().single { it.value == value }
    }

    class GsonAdapter : TypeAdapter<CoronaTestResult>() {
        override fun write(out: JsonWriter, value: CoronaTestResult?) {
            if (value == null) out.nullValue()
            else out.value(value.value)
        }

        override fun read(reader: JsonReader): CoronaTestResult? = when (reader.peek()) {
            JsonToken.NULL -> reader.nextNull().let { null }
            else -> fromInt(reader.nextInt())
        }
    }
}

val CoronaTestResult?.isFinalResult
    get() = when (this) {
        CoronaTestResult.PCR_POSITIVE,
        CoronaTestResult.PCR_NEGATIVE,
        CoronaTestResult.RAT_POSITIVE,
        CoronaTestResult.RAT_NEGATIVE -> true
        else -> false
    }

val CoronaTestResult?.isPending
    get() = this == CoronaTestResult.PCR_OR_RAT_PENDING
