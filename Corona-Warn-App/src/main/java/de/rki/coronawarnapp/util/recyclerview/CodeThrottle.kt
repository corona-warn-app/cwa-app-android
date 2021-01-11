package de.rki.coronawarnapp.util.recyclerview

class CodeThrottle {
    companion object {
        const val MIN_INTERVAL = 300
    }
    private var lastEventTime = System.currentTimeMillis()

    fun throttle(code: () -> Unit) {
        val eventTime = System.currentTimeMillis()
        if (eventTime - lastEventTime > MIN_INTERVAL) {
            lastEventTime = eventTime
            code()
        }
    }
}
