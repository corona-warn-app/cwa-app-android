package de.rki.coronawarnapp.dccticketing.core.allowlist.repo

import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingAllowListContainer
import de.rki.coronawarnapp.dccticketing.core.allowlist.server.DccTicketingAllowListServer
import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingServiceProviderAllowListEntry
import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingValidationServiceAllowListEntry
import de.rki.coronawarnapp.dccticketing.core.allowlist.internal.DccTicketingAllowListParser
import de.rki.coronawarnapp.dccticketing.core.allowlist.repo.storage.DccTicketingAllowListStorage
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.plus
import org.joda.time.Duration
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccTicketingAllowListRepository @Inject constructor(
    @AppScope appScope: CoroutineScope,
    dispatcherProvider: DispatcherProvider,
    private val dccTicketingAllowListServer: DccTicketingAllowListServer,
    private val dccTicketingAllowListStorage: DccTicketingAllowListStorage,
    private val dccTicketingAllowListParser: DccTicketingAllowListParser
) {

    private val internalData: HotDataFlow<DccTicketingAllowListContainer> = HotDataFlow(
        loggingTag = TAG,
        scope = appScope + dispatcherProvider.IO,
        sharingBehavior = SharingStarted.WhileSubscribed(
            stopTimeoutMillis = Duration.standardSeconds(5).millis,
            replayExpirationMillis = 0
        ),
        startValueProvider = { dccTicketingAllowListStorage.load() }
    )

    init {
        internalData.data
            .onStart { Timber.tag(TAG).d("Observing allow list") }
            .drop(1)
            .onEach {
                Timber.tag(TAG).v("Storing %s", it)
                dccTicketingAllowListStorage.save(value = it)
            }
            .catch {
                Timber.tag(TAG).e(it, "Storing allow list data failed")
                throw it
            }
            .launchIn(scope = appScope + dispatcherProvider.IO)
    }

    val validationServiceAllowList: Flow<Set<DccTicketingValidationServiceAllowListEntry>> = internalData.data
        .map { it.validationServiceAllowList }

    val serviceProviderAllowList: Flow<Set<DccTicketingServiceProviderAllowListEntry>> = internalData.data
        .map { it.serviceProviderAllowList }

    suspend fun refresh() = internalData.updateBlocking {
        Timber.tag(TAG).d("refresh() - start")
        tryGetAndParse() ?: this
    }.also {
        Timber.tag(TAG).d("refresh() - returning %s", it)
    }

    suspend fun clear() {
        Timber.tag(TAG).d("clear()")
        internalData.updateBlocking {
            DccTicketingAllowListContainer()
        }
    }

    private suspend fun tryGetAndParse(): DccTicketingAllowListContainer? = try {
        Timber.tag(TAG).d("tryGetAndParse()")
        val rawData = dccTicketingAllowListServer.getAllowlist()
        dccTicketingAllowListParser.parse(rawData = rawData)
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "Failed to get new allow list.")
        null
    }.also { Timber.tag(TAG).d("Returning %S", it) }

    companion object {
        private val TAG = tag<DccTicketingAllowListRepository>()
    }
}
