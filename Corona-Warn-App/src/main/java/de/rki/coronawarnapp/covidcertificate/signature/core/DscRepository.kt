package de.rki.coronawarnapp.covidcertificate.signature.core

import de.rki.coronawarnapp.covidcertificate.signature.core.server.DscServer
import de.rki.coronawarnapp.covidcertificate.signature.core.storage.DefaultDscSource
import de.rki.coronawarnapp.covidcertificate.signature.core.storage.LocalDscStorage
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.plus
import org.joda.time.Duration
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DscRepository @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
    private val defaultDscData: DefaultDscSource,
    private val localStorage: LocalDscStorage,
    private val dscServer: DscServer,
    private val dscDataParser: DscDataParser,
) {
    private val internalData: HotDataFlow<DscData> = HotDataFlow(
        loggingTag = TAG,
        scope = appScope + dispatcherProvider.Default,
        sharingBehavior = SharingStarted.WhileSubscribed(
            stopTimeoutMillis = Duration.standardSeconds(5).millis,
            replayExpirationMillis = 0
        ),
    ) {

        // TODO
        DscData(
            dscList = emptyList()
        )
    }

    val dscData = internalData.data

    suspend fun refresh() {
        Timber.tag(TAG).d("refresh()")
    }

    companion object {
        private const val TAG = "DscRepository"
    }
}
