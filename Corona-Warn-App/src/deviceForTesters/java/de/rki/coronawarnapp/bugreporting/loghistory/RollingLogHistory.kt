package de.rki.coronawarnapp.bugreporting.loghistory

import android.util.Log
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class RollingLogHistory @Inject constructor(
    @AppScope private val scope: CoroutineScope,
    dispatcherProvider: DispatcherProvider
) : Timber.DebugTree() {

    private val bufferLock = Mutex()
    private val buffer: ArrayDeque<String> = ArrayDeque(BUFFER_SIZE + 1)
    private val logQueue = MutableStateFlow("")

    init {
        logQueue
            .filter { it.isNotBlank() }
            .onEach {
                bufferLock.withLock {
                    buffer.addFirst(it)
                    if (buffer.size > BUFFER_SIZE) {
                        buffer.removeLast()
                    }
                }
            }
            .launchIn(scope + dispatcherProvider.IO)
    }

    suspend fun getLoglines(count: Int): List<String> = bufferLock.withLock {
        buffer.subList(0, min(count, buffer.size))
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val formatLine =
            "${System.currentTimeMillis()}  ${priorityToString(priority)}/$tag: $message\n"
        logQueue.value = formatLine
    }

    companion object {
        private const val BUFFER_SIZE = 200
        private fun priorityToString(priority: Int): String = when (priority) {
            Log.ERROR -> "E"
            Log.WARN -> "W"
            Log.INFO -> "I"
            Log.DEBUG -> "D"
            Log.VERBOSE -> "V"
            else -> priority.toString()
        }
    }
}
