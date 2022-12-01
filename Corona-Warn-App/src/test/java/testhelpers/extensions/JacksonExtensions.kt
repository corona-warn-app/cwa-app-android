package testhelpers.extensions

import de.rki.coronawarnapp.util.serialization.SerializationModule

fun String.toComparableJsonPretty1(): String = try {
    val mapper = SerializationModule.jacksonBaseMapper
    mapper.readTree(this).toPrettyString()
} catch (e: Exception) {
    throw IllegalArgumentException("'$this' wasn't valid JSON")
}
