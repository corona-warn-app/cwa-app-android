package de.rki.coronawarnapp.bugreporting.debuglog

import de.rki.coronawarnapp.util.threads.NamedThreadFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import javax.inject.Qualifier
import kotlin.coroutines.CoroutineContext

object DebugLoggerScope : CoroutineScope {
    val dispatcher = Executors.newSingleThreadExecutor(
        NamedThreadFactory("DebugLogger")
    ).asCoroutineDispatcher()
    override val coroutineContext: CoroutineContext = SupervisorJob() + dispatcher
}

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class DebuggerScope
