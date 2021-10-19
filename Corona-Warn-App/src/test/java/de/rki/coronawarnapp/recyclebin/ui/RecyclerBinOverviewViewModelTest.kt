package de.rki.coronawarnapp.recyclebin.ui

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.reyclebin.RecycledItemsProvider
import de.rki.coronawarnapp.reyclebin.ui.RecyclerBinEvent
import de.rki.coronawarnapp.reyclebin.ui.RecyclerBinOverviewViewModel
import de.rki.coronawarnapp.reyclebin.ui.adapter.OverviewSubHeaderItem
import de.rki.coronawarnapp.reyclebin.ui.adapter.RecoveryCertificateCard
import de.rki.coronawarnapp.reyclebin.ui.adapter.TestCertificateCard
import de.rki.coronawarnapp.reyclebin.ui.adapter.VaccinationCertificateCard
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue

@ExtendWith(InstantExecutorExtension::class)
class RecyclerBinOverviewViewModelTest : BaseTest() {

    @RelaxedMockK private lateinit var recycledItemsProvider: RecycledItemsProvider

    private val recCert: RecoveryCertificate = mockk {
        every { containerId } returns RecoveryCertificateContainerId("recCert")
    }

    private val testCert: TestCertificate = mockk {
        every { containerId } returns TestCertificateContainerId("testCert")
    }

    private val vaccCert: VaccinationCertificate = mockk {
        every { containerId } returns VaccinationCertificateContainerId("vaccCert")
    }

    private val cwaCert: CwaCovidCertificate = mockk {
        every { containerId } returns mockk()
    }

    private val instance: RecyclerBinOverviewViewModel
        get() = RecyclerBinOverviewViewModel(
            dispatcherProvider = TestDispatcherProvider(),
            recycledItemsProvider = recycledItemsProvider
        )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { recycledItemsProvider.recycledCertificates } returns flowOf(setOf(recCert, testCert, vaccCert))
    }

    @Test
    fun `No recycled items - empty list`() {
        every { recycledItemsProvider.recycledCertificates } returns flowOf(emptySet())
        instance.listItems.getOrAwaitValue() shouldBe emptyList()
    }

    @Test
    fun `Creates list with sub header and cert items and removes unknown types`() {
        every { recycledItemsProvider.recycledCertificates } returns flowOf(setOf(recCert, testCert, vaccCert, cwaCert))

        val items = instance.listItems.getOrAwaitValue()
        items.size shouldBe 4
        items[0] should beInstanceOf<OverviewSubHeaderItem>()
        items[1] should beInstanceOf<RecoveryCertificateCard.Item>()
        items[2] should beInstanceOf<TestCertificateCard.Item>()
        items[3] should beInstanceOf<VaccinationCertificateCard.Item>()
    }

    @Test
    fun `Cert items trigger expected events`() {
        with(instance) {
            val items = listItems.getOrAwaitValue()

            (items[1] as RecoveryCertificateCard.Item).also {
                val cert = recCert
                val pos = 1
                it.certificate shouldBe cert
                it.onRemove(cert, pos)
                events.getOrAwaitValue() shouldBe RecyclerBinEvent.RemoveItem(cert, pos)
                it.onRestore(cert)
                events.getOrAwaitValue() shouldBe RecyclerBinEvent.ConfirmRestoreItem(cert)
            }

            (items[2] as TestCertificateCard.Item).also {
                val cert = testCert
                val pos = 2
                it.certificate shouldBe cert
                it.onRemove(cert, pos)
                events.getOrAwaitValue() shouldBe RecyclerBinEvent.RemoveItem(cert, pos)
                it.onRestore(cert)
                events.getOrAwaitValue() shouldBe RecyclerBinEvent.ConfirmRestoreItem(cert)
            }

            (items[3] as VaccinationCertificateCard.Item).also {
                val cert = vaccCert
                val pos = 3
                it.certificate shouldBe cert
                it.onRemove(cert, pos)
                events.getOrAwaitValue() shouldBe RecyclerBinEvent.RemoveItem(cert, pos)
                it.onRestore(cert)
                events.getOrAwaitValue() shouldBe RecyclerBinEvent.ConfirmRestoreItem(cert)
            }
        }
    }

    @Test
    fun `onRemoveAllItemsClicked triggers ConfirmRemoveAll event`() {
        with(instance) {
            onRemoveAllItemsClicked()
            events.getOrAwaitValue() shouldBe RecyclerBinEvent.ConfirmRemoveAll
        }
    }

    @Test
    fun `onRemoveAllItemsConfirmation collects recycled items and triggers their deletion`() {
        val containerIds = listOf(recCert.containerId, testCert.containerId, vaccCert.containerId)

        instance.onRemoveAllItemsConfirmation()

        coVerify {
            with(recycledItemsProvider) {
                recycledCertificates
                deleteAllCertificate(containerIds)
            }
        }
    }

    @Test
    fun `onRemoveItem triggers deletion`() {
        val containerId = testCert.containerId
        instance.onRemoveItem(testCert)

        coVerify {
            recycledItemsProvider.deleteCertificate(containerId)
        }
    }

    @Test
    fun `onRestoreConfirmation triggers restoration`() {
        val containerId = testCert.containerId
        instance.onRestoreConfirmation(testCert)

        coVerify {
            recycledItemsProvider.restoreCertificate(containerId)
        }
    }
}
