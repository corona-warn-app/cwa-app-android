package testhelpers.extensions

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.mockwebserver.MockResponse

fun String.toComparableJsonPretty1(): String = try {
    ObjectMapper().readTree(this).toPrettyString()
} catch (e: Exception) {
    throw IllegalArgumentException("'$this' wasn't valid JSON")
}

fun String.toComparableJson1() = try {
    ObjectMapper().readTree(this).toString()
} catch (e: Exception) {
    throw IllegalArgumentException("'$this' wasn't valid JSON")
}

fun String.toJsonResponse1(): MockResponse = MockResponse().setBody(this.toComparableJson1())
