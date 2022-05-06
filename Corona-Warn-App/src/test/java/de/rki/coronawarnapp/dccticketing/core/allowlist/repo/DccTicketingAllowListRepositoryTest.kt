package de.rki.coronawarnapp.dccticketing.core.allowlist.repo

import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingAllowListContainer
import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingServiceProviderAllowListEntry
import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingValidationServiceAllowListEntry
import de.rki.coronawarnapp.dccticketing.core.allowlist.internal.DccTicketingAllowListParser
import de.rki.coronawarnapp.dccticketing.core.allowlist.repo.storage.DccTicketingAllowListStorage
import de.rki.coronawarnapp.dccticketing.core.allowlist.server.DccTicketingAllowListServer
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.coroutines.runTest2

class DccTicketingAllowListRepositoryTest : BaseTest() {

    @MockK lateinit var dccTicketingAllowListServer: DccTicketingAllowListServer
    @RelaxedMockK lateinit var dccTicketingAllowListStorage: DccTicketingAllowListStorage
    @MockK lateinit var dccTicketingAllowListParser: DccTicketingAllowListParser

    private val validationServiceSet = setOf(
        DccTicketingValidationServiceAllowListEntry(
            serviceProvider = "serviceProvider",
            hostname = "eu.service.com",
            fingerprint256 = "fingerprint256".decodeBase64()!!
        )
    )

    private val serviceProviderSet = setOf(
        DccTicketingServiceProviderAllowListEntry(
            serviceIdentityHash = "serviceIdentityHash".decodeBase64()!!
        )
    )

    private val container = DccTicketingAllowListContainer(
        serviceProviderAllowList = serviceProviderSet,
        validationServiceAllowList = validationServiceSet
    )

    private fun createInstance(scope: CoroutineScope) = DccTicketingAllowListRepository(
        appScope = scope,
        dispatcherProvider = TestDispatcherProvider(),
        dccTicketingAllowListServer = dccTicketingAllowListServer,
        dccTicketingAllowListStorage = dccTicketingAllowListStorage,
        dccTicketingAllowListParser = dccTicketingAllowListParser
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery { dccTicketingAllowListStorage.load() } returns null
        coEvery { dccTicketingAllowListServer.getAllowlist() } returns byteArrayOf()
        coEvery { dccTicketingAllowListParser.parse(any()) } returns container
    }

    @Test
    fun `returns empty container if storage is empty`() = runTest2 {
        with(createInstance(scope = this)) {
            validationServiceAllowList.first().shouldBeEmpty()
            serviceProviderAllowList.first().shouldBeEmpty()
        }

        coVerify {
            dccTicketingAllowListParser wasNot Called
            dccTicketingAllowListStorage.load()
        }
    }

    @Test
    fun `clear() clears internal data and storage`() = runTest2 {
        with(createInstance(scope = this)) {
            refresh()
            validationServiceAllowList.first().shouldNotBeEmpty()
            serviceProviderAllowList.first().shouldNotBeEmpty()

            clear()
            validationServiceAllowList.first().shouldBeEmpty()
            serviceProviderAllowList.first().shouldBeEmpty()
        }

        coVerify {
            dccTicketingAllowListStorage.clear()
        }
    }

    @Test
    fun `fallbacks to empty container if initial data loading fails`() = runTest2 {
        coEvery { dccTicketingAllowListStorage.load() } returns byteArrayOf()
        coEvery { dccTicketingAllowListParser.parse(rawData = any()) } throws Exception("Test Error")

        with(createInstance(scope = this)) {
            validationServiceAllowList.first().shouldBeEmpty()
            serviceProviderAllowList.first().shouldBeEmpty()
        }
    }

    @Test
    fun `fallbacks to last container if anything fails`() = runTest2 {
        coEvery { dccTicketingAllowListStorage.load() } returns byteArrayOf()

        val instance = createInstance(scope = this)

        with(instance) {
            refresh() shouldBe container
            validationServiceAllowList.first() shouldBe validationServiceSet
            serviceProviderAllowList.first() shouldBe serviceProviderSet
        }

        coEvery { dccTicketingAllowListParser.parse(rawData = any()) } throws Exception("Test Error")

        with(instance) {
            refresh() shouldBe container
            validationServiceAllowList.first() shouldBe validationServiceSet
            serviceProviderAllowList.first() shouldBe serviceProviderSet
        }

        coEvery { dccTicketingAllowListServer.getAllowlist() } throws Exception("Test Error")

        with(instance) {
            refresh() shouldBe container
            validationServiceAllowList.first() shouldBe validationServiceSet
            serviceProviderAllowList.first() shouldBe serviceProviderSet
        }
    }

    @Test
    fun `updates internal data and storage with new data`() = runTest2 {
        val container2 = container.copy(serviceProviderAllowList = emptySet())
        val container3 = container.copy(validationServiceAllowList = emptySet())

        val data = container.toString().toByteArray()
        val data2 = container2.toString().toByteArray()
        val data3 = container3.toString().toByteArray()

        with(createInstance(scope = this)) {
            checkUpdatesDataAndStorage(data = data, container = container)
            checkUpdatesDataAndStorage(data = data2, container = container2)
            checkUpdatesDataAndStorage(data = data3, container = container3)
        }

        coVerifyOrder {
            dccTicketingAllowListStorage.save(data = data)
            dccTicketingAllowListStorage.save(data = data2)
            dccTicketingAllowListStorage.save(data = data3)
        }
    }

    private suspend fun DccTicketingAllowListRepository.checkUpdatesDataAndStorage(
        data: ByteArray,
        container: DccTicketingAllowListContainer
    ) {
        coEvery { dccTicketingAllowListServer.getAllowlist() } returns data
        coEvery { dccTicketingAllowListParser.parse(data) } returns container

        refresh() shouldBe container
        validationServiceAllowList.first() shouldBe container.validationServiceAllowList
        serviceProviderAllowList.first() shouldBe container.serviceProviderAllowList

        coVerify {
            dccTicketingAllowListStorage.save(data = data)
        }
    }
}
