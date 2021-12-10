package de.rki.coronawarnapp.dccticketing.core.allowlist.internal

import com.google.protobuf.InvalidProtocolBufferException
import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingAllowListContainer
import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingServiceProviderAllowListEntry
import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingValidationServiceAllowListEntry
import de.rki.coronawarnapp.server.protocols.internal.dgc.ValidationServiceAllowlistOuterClass.ServiceProviderAllowlistItem
import de.rki.coronawarnapp.server.protocols.internal.dgc.ValidationServiceAllowlistOuterClass.ValidationServiceAllowlist
import de.rki.coronawarnapp.server.protocols.internal.dgc.ValidationServiceAllowlistOuterClass.ValidationServiceAllowlistItem
import de.rki.coronawarnapp.util.toProtoByteString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DccTicketingAllowListParserTest : BaseTest() {

    private val instance: DccTicketingAllowListParser
        get() = DccTicketingAllowListParser()

    @Test
    fun `creates empty container from empty allow list`() {
        val emptyAllowList = ValidationServiceAllowlist.getDefaultInstance()
        val emptyDccTicketingAllowListContainer = DccTicketingAllowListContainer()

        instance.parse(emptyAllowList.toByteArray()) shouldBe emptyDccTicketingAllowListContainer
    }

    @Test
    fun `allow list mapping is correct`() {
        val serviceIdentityHash = "serviceIdentityHash".decodeBase64()!!
        val serviceProvider = "serviceProvider"
        val hostname = "eu.service.com"
        val fingerprint256 = "fingerprint256".decodeBase64()!!

        val validationServiceAllowlistItem = ValidationServiceAllowlistItem.newBuilder()
            .setServiceProvider(serviceProvider)
            .setHostname(hostname)
            .setFingerprint256(fingerprint256.toProtoByteString())
            .build()

        val serviceProviderAllowlistItem = ServiceProviderAllowlistItem.newBuilder()
            .setServiceIdentityHash(serviceIdentityHash.toProtoByteString())
            .build()

        val validationServiceAllowlist = ValidationServiceAllowlist.newBuilder()
            .addCertificates(validationServiceAllowlistItem)
            .addServiceProviders(serviceProviderAllowlistItem)
            .build()

        val validationServiceAllowListEntry = DccTicketingValidationServiceAllowListEntry(
            serviceProvider = serviceProvider,
            hostname = hostname,
            fingerprint256 = fingerprint256
        )
        val serviceProviderAllowListEntry =
            DccTicketingServiceProviderAllowListEntry(serviceIdentityHash = serviceIdentityHash)
        val dccTicketingAllowListContainer = DccTicketingAllowListContainer(
            serviceProviderAllowList = setOf(serviceProviderAllowListEntry),
            validationServiceAllowList = setOf(validationServiceAllowListEntry)
        )

        instance.parse(validationServiceAllowlist.toByteArray()) shouldBe dccTicketingAllowListContainer
    }

    @Test
    fun `throws on faulty data`() {
        val rawData = "Not a ValidationServiceAllowlist".toByteArray(Charsets.ISO_8859_1)

        shouldThrow<InvalidProtocolBufferException> {
            instance.parse(rawData = rawData)
        }
    }
}
