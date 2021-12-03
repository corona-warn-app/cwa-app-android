package de.rki.coronawarnapp.dccticketing.core.allowlist.internal

import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingAllowListContainer
import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingServiceProviderAllowListEntry
import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingValidationServiceAllowListEntry
import de.rki.coronawarnapp.server.protocols.internal.dgc.ValidationServiceAllowlistOuterClass
import de.rki.coronawarnapp.util.toOkioByteString
import timber.log.Timber

internal fun ValidationServiceAllowlist.toAllowListContainer(): DccTicketingAllowListContainer {
    Timber.tag(TAG).d("Mapping %s to allow list container", this)
    return DccTicketingAllowListContainer(
        serviceProviderAllowList = serviceProvidersList.toServiceProviderAllowList().toSet(),
        validationServiceAllowList = certificatesList.toValidationServiceAllowList().toSet()
    ).also { Timber.tag(TAG).d("Created %s", it) }
}

internal fun List<ServiceProviderAllowlistItem>.toServiceProviderAllowList(): List<DccTicketingServiceProviderAllowListEntry> =
    map { it.toServiceProviderAllowListEntry() }

internal fun ServiceProviderAllowlistItem.toServiceProviderAllowListEntry() = DccTicketingServiceProviderAllowListEntry(
    serviceIdentityHash = serviceIdentityHash.toOkioByteString()
)

internal fun List<ValidationServiceAllowlistItem>.toValidationServiceAllowList(): List<DccTicketingValidationServiceAllowListEntry> =
    map { it.toValidationServiceAllowListEntry() }

internal fun ValidationServiceAllowlistItem.toValidationServiceAllowListEntry() =
    DccTicketingValidationServiceAllowListEntry(
        serviceProvider = serviceProvider,
        hostname = hostname,
        fingerprint256 = fingerprint256.toOkioByteString()
    )

private typealias ValidationServiceAllowlist = ValidationServiceAllowlistOuterClass.ValidationServiceAllowlist
private typealias ServiceProviderAllowlistItem = ValidationServiceAllowlistOuterClass.ServiceProviderAllowlistItem
private typealias ValidationServiceAllowlistItem = ValidationServiceAllowlistOuterClass.ValidationServiceAllowlistItem

private const val TAG = "DccTicketingAllowListMapper"
