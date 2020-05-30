package de.rki.coronawarnapp.util.formatter

@Suppress("MagicNumber")
enum class TestResult(val value: Int) {
    PENDING(0),
    NEGATIVE(1),
    POSITIVE(2),
    INVALID(3);

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }
}
