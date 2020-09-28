package de.rki.coronawarnapp.environment

open class BaseEnvironmentModule {
    fun requireValidUrl(url: String) {
        if (!url.startsWith("https://")) throw IllegalStateException("Invalid: $url")
    }
}
