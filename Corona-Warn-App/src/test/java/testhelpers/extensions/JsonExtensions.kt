package testhelpers.extensions

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.kotest.assertions.assertionCounter
import io.kotest.assertions.collectOrThrow
import io.kotest.assertions.eq.eq
import io.kotest.assertions.errorCollector
import okhttp3.mockwebserver.MockResponse

private fun String.determineJsonType(): Class<out JsonElement> = when {
    this.trimStart().startsWith("[") -> JsonArray::class.java
    else -> JsonObject::class.java
}

fun String.toComparableJson() = try {
    Gson().fromJson(this, this.determineJsonType()).toString()
} catch (e: Exception) {
    throw IllegalArgumentException("'$this' wasn't valid JSON")
}

fun String.toComparableJsonPretty(): String = try {
    val gson = GsonBuilder().setPrettyPrinting().create()
    val obj = gson.fromJson(this, this.determineJsonType())
    gson.toJson(obj)
} catch (e: Exception) {
    throw IllegalArgumentException("'$this' wasn't valid JSON")
}

@Suppress("UNCHECKED_CAST")
infix fun String.shouldMatchJson(expected: String) {
    val actualPretty = this.toComparableJsonPretty()
    val expectedPretty = expected.toComparableJsonPretty()

    assertionCounter.inc()
    eq(actualPretty, expectedPretty)?.let(errorCollector::collectOrThrow)
}

fun String.toJsonResponse(): MockResponse = MockResponse().setBody(this.toComparableJson())
