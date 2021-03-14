package de.rki.coronawarnapp.tracing

sealed class TracingProgress {

    object Idle : TracingProgress()

    object Downloading : TracingProgress()

    object ENFIsCalculating : TracingProgress()
}
