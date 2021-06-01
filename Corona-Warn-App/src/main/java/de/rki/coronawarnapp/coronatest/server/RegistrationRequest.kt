package de.rki.coronawarnapp.coronatest.server

import de.rki.coronawarnapp.coronatest.type.common.DateOfBirthKey

data class RegistrationRequest(
    val key: String,
    val type: VerificationKeyType,
    val dateOfBirthKey: DateOfBirthKey? = null,
)
