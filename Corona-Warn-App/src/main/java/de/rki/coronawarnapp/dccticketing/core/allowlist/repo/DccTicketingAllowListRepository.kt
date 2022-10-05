package de.rki.coronawarnapp.dccticketing.core.allowlist.repo

import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingAllowListContainer
import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingServiceProviderAllowListEntry
import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingValidationServiceAllowListEntry
import de.rki.coronawarnapp.dccticketing.core.allowlist.internal.DccTicketingAllowListParser
import de.rki.coronawarnapp.dccticketing.core.allowlist.repo.storage.DccTicketingAllowListStorage
import de.rki.coronawarnapp.dccticketing.core.allowlist.server.DccTicketingAllowListServer
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.HotDataFlow
import de.rki.coronawarnapp.util.reset.Resettable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.plus
import timber.log.Timber
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DccTicketingAllowListRepository @Inject constructor(
    @AppScope appScope: CoroutineScope,
    dispatcherProvider: DispatcherProvider,
    private val dccTicketingAllowListServer: DccTicketingAllowListServer,
    private val dccTicketingAllowListStorage: DccTicketingAllowListStorage,
    private val dccTicketingAllowListParser: DccTicketingAllowListParser
) : Resettable {

    private val emptyDccTicketingAllowListContainer: DccTicketingAllowListContainer
        get() = DccTicketingAllowListContainer()

    private val internalData: HotDataFlow<DccTicketingAllowListContainer> = HotDataFlow(
        loggingTag = TAG,
        scope = appScope + dispatcherProvider.IO,
        sharingBehavior = SharingStarted.WhileSubscribed(
            stopTimeoutMillis = Duration.ofMinutes(5).toMillis()
        ),
        startValueProvider = { loadInitialData() }
    )

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

    override suspend fun reset() {
        Timber.tag(TAG).d("reset()")
        internalData.updateBlocking {
            emptyDccTicketingAllowListContainer
        }
    }

    private suspend fun tryGetAndParse(): DccTicketingAllowListContainer? = try {
        Timber.tag(TAG).d("tryGetAndParse()")
        val rawData = dccTicketingAllowListServer.getAllowlist()
        rawData.toAllowListContainer()
            .also { dccTicketingAllowListStorage.save(data = rawData) }
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "Failed to get new allow list.")
        null
    }.also { Timber.tag(TAG).d("Returning %s", it) }

    private fun ByteArray?.toAllowListContainer(): DccTicketingAllowListContainer = when (this != null) {
        true -> dccTicketingAllowListParser.parse(rawData = this)
        false -> {
            Timber.tag(TAG).d("No data to parse. Returning empty allow list container")
            emptyDccTicketingAllowListContainer
        }
    }

    private suspend fun loadInitialData(): DccTicketingAllowListContainer = try {
        Timber.tag(TAG).d("loadInitialData()")
        dccTicketingAllowListStorage.load().toAllowListContainer()
    } catch (e: Exception) {
        Timber.tag(TAG).w(e, "Failed to load initial data. Returning empty allow list container")
        emptyDccTicketingAllowListContainer
    }

    companion object {
        private val TAG = tag<DccTicketingAllowListRepository>()
    }
}
