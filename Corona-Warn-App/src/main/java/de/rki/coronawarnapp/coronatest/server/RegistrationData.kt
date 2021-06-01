package de.rki.coronawarnapp.coronatest.server

data class RegistrationData(
    val registrationToken: String,
    val testResultResponse: CoronaTestResultResponse
)
