package de.rki.coronawarnapp.environment

open class BaseEnvironmentModule {
    fun requireValidUrl(url: String): String {
        if (!url.startsWith("https://")) {
            throw IllegalArgumentException("HTTPS url required: $url")
        }
        return url
    }
}
