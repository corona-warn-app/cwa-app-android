package de.rki.coronawarnapp.coronatest.type.common

import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

data class DateOfBirthKey constructor(
    private val testGuid: String,
    private val dateOfBirth: LocalDate,
) {

    init {
        require(testGuid.isNotEmpty()) { "GUID can't be empty." }
    }

    val key by lazy {
        val dobFormatted = dateOfBirth.toString(DOB_FORMATTER)
        val keyHash = "${testGuid}$dobFormatted".toSHA256()
        "x${keyHash.substring(1)}"
    }

    companion object {
        private val DOB_FORMATTER = DateTimeFormat.forPattern("ddMMYYYY")
    }
}
