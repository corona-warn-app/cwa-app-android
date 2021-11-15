package de.rki.coronawarnapp.dccticketing.core.server

import dagger.Lazy
import dagger.Reusable
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingServiceIdentityDocument
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DccTicketingValidationServer @Inject constructor(
    private val dccTicketingValidationApiV1Lazy: Lazy<DccTicketingApiV1>,
    private val dispatcherProvider: DispatcherProvider,
) {

    private val dccTicketingValidationApiV1: DccTicketingApiV1
        get() = dccTicketingValidationApiV1Lazy.get()

    suspend fun getServiceIdentityDocument(url: String): DccTicketingServiceIdentityDocument =
        withContext(dispatcherProvider.IO) {
            Timber.d("getServiceIdentityDocument(url=%s)", url)
            dccTicketingValidationApiV1.getServiceIdentityDocument(url)
        }
}
