package de.rki.coronawarnapp.tracing

sealed class TracingProgress {

    object Idle : TracingProgress() {
        override fun toString(): String = "TracingProgress.Idle"
    }

    object Downloading : TracingProgress() {
        override fun toString(): String = "TracingProgress.Downloading"
    }

    object IsCalculating : TracingProgress() {
        override fun toString(): String = "TracingProgress.IsCalculating"
    }
}
