package de.rki.coronawarnapp.exception

import java.lang.Exception

class ApplicationConfigurationCorruptException : Exception(
    "the application configuration is corrupt"
)
