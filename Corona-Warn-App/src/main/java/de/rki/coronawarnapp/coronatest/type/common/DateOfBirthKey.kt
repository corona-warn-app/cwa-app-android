package de.rki.coronawarnapp.coronatest.type.common

import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class DateOfBirthKey constructor(
    private val testGuid: String,
    private val dateOfBirth: LocalDate,
) {

    init {
        require(testGuid.isNotEmpty()) { "GUID can't be empty." }
    }

    val key by lazy {
        val dobFormatted = dateOfBirth.format(DOB_FORMATTER)
        val keyHash = "${testGuid}$dobFormatted".toSHA256()
        "x${keyHash.substring(1)}"
    }

    companion object {
        private val DOB_FORMATTER = DateTimeFormatter.ofPattern("ddMMyyyy")
    }
}
