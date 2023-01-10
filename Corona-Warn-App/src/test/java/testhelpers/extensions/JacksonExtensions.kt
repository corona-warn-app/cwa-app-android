package testhelpers.extensions

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.assertionCounter
import io.kotest.assertions.collectOrThrow
import io.kotest.assertions.eq.eq
import io.kotest.assertions.errorCollector
import okhttp3.mockwebserver.MockResponse

fun String.toComparableJsonPretty(): String = try {
    ObjectMapper().readTree(this).toPrettyString()
} catch (e: Exception) {
    throw IllegalArgumentException("'$this' wasn't valid JSON")
}

fun String.toComparableJson() = try {
    ObjectMapper().readTree(this).toString()
} catch (e: Exception) {
    throw IllegalArgumentException("'$this' wasn't valid JSON")
}

fun String.toJsonResponse(): MockResponse = MockResponse().setBody(this.toComparableJson())

infix fun String.shouldMatchJson1(expected: String) {
    val actualPretty = this.toComparableJsonPretty()
    val expectedPretty = expected.toComparableJsonPretty()

    assertionCounter.inc()
    eq(actualPretty, expectedPretty)?.let(errorCollector::collectOrThrow)
}
