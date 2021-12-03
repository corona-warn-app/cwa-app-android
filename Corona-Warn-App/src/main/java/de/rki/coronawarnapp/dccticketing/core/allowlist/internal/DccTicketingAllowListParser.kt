package de.rki.coronawarnapp.dccticketing.core.allowlist.internal

import dagger.Reusable
import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingAllowListContainer
import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingServiceProviderAllowListEntry
import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingValidationServiceAllowListEntry
import de.rki.coronawarnapp.server.protocols.internal.dgc.ValidationServiceAllowlistOuterClass
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.toOkioByteString
import timber.log.Timber
import javax.inject.Inject

@Reusable
class DccTicketingAllowListParser @Inject constructor() {

    fun parse(rawData: ByteArray): DccTicketingAllowListContainer {
        Timber.tag(TAG).d("Creating DccTicketingAllowListContainer from raw data")
        return ValidationServiceAllowlist.parseFrom(rawData)
            .toAllowListContainer()
            .also { Timber.tag(TAG).d("Created %s", it) }
    }

    private fun ValidationServiceAllowlist.toAllowListContainer() = DccTicketingAllowListContainer(
        serviceProviderAllowList = serviceProvidersList.toServiceProviderAllowList().toSet(),
        validationServiceAllowList = certificatesList.toValidationServiceAllowList().toSet()
    )

    private fun List<ServiceProviderAllowlistItem>.toServiceProviderAllowList(): List<DccTicketingServiceProviderAllowListEntry> =
        map { it.toServiceProviderAllowListEntry() }

    private fun ServiceProviderAllowlistItem.toServiceProviderAllowListEntry() =
        DccTicketingServiceProviderAllowListEntry(
            serviceIdentityHash = serviceIdentityHash.toOkioByteString()
        )

    private fun List<ValidationServiceAllowlistItem>.toValidationServiceAllowList(): List<DccTicketingValidationServiceAllowListEntry> =
        map { it.toValidationServiceAllowListEntry() }

    private fun ValidationServiceAllowlistItem.toValidationServiceAllowListEntry() =
        DccTicketingValidationServiceAllowListEntry(
            serviceProvider = serviceProvider,
            hostname = hostname,
            fingerprint256 = fingerprint256.toOkioByteString()
        )

    companion object {
        private val TAG = tag<DccTicketingAllowListParser>()
    }
}

private typealias ValidationServiceAllowlist = ValidationServiceAllowlistOuterClass.ValidationServiceAllowlist
private typealias ServiceProviderAllowlistItem = ValidationServiceAllowlistOuterClass.ServiceProviderAllowlistItem
private typealias ValidationServiceAllowlistItem = ValidationServiceAllowlistOuterClass.ValidationServiceAllowlistItem

