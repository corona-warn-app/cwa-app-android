package de.rki.coronawarnapp.bugreporting.debuglog

import de.rki.coronawarnapp.util.TimeStamper
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import timber.log.Timber
import javax.inject.Inject

class DebugLogTree @Inject constructor(
    private val timeStamper: TimeStamper
) : Timber.DebugTree() {

    private val logLinesPub = MutableSharedFlow<LogLine>(
        replay = 10,
        extraBufferCapacity = 1024,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val logLines: Flow<LogLine> = logLinesPub

    init {
        Timber.tag(TAG).d("init()")
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        LogLine(
            timestamp = timeStamper.nowUTC,
            priority = priority,
            tag = tag,
            message = message,
            throwable = t
        ).also { logLinesPub.tryEmit(it) }
    }

    companion object {
        private const val TAG = "DebugLogTree"
    }
}
