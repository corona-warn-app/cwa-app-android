package de.rki.coronawarnapp.datadonation.safetynet

class SafetyNetException constructor(
    message: String?,
    cause: Throwable?
) : Exception(message, cause)
