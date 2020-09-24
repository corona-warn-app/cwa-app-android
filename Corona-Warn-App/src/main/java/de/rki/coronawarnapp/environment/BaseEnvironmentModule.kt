package de.rki.coronawarnapp.environment

abstract class BaseEnvironmentModule {
    fun requireValidUrl(url: String) {
        if (!url.startsWith("https://")) throw IllegalStateException("Invalid: $url")
    }
}
