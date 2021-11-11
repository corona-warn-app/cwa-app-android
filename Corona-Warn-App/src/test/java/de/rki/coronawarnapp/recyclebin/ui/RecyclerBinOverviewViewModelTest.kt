package de.rki.coronawarnapp.recyclebin.ui

import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.reyclebin.coronatest.request.toRestoreRecycledTestRequest
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsProvider
import de.rki.coronawarnapp.reyclebin.covidcertificate.RecycledCertificatesProvider
import de.rki.coronawarnapp.reyclebin.ui.RecyclerBinEvent
import de.rki.coronawarnapp.reyclebin.ui.RecyclerBinOverviewViewModel
import de.rki.coronawarnapp.reyclebin.ui.adapter.OverviewSubHeaderItem
import de.rki.coronawarnapp.reyclebin.ui.adapter.RecoveryCertificateCard
import de.rki.coronawarnapp.reyclebin.ui.adapter.TestCertificateCard
import de.rki.coronawarnapp.reyclebin.ui.adapter.VaccinationCertificateCard
import de.rki.coronawarnapp.submission.SubmissionRepository
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue

@ExtendWith(InstantExecutorExtension::class)
class RecyclerBinOverviewViewModelTest : BaseTest() {

    @RelaxedMockK private lateinit var recycledCertificatesProvider: RecycledCertificatesProvider
    @RelaxedMockK private lateinit var recycledCoronaTestsProvider: RecycledCoronaTestsProvider
    @RelaxedMockK private lateinit var submissionRepository: SubmissionRepository

    private val recycledRAT = RACoronaTest(
        identifier = "rat-identifier",
        lastUpdatedAt = Instant.EPOCH,
        registeredAt = Instant.EPOCH,
        registrationToken = "token",
        testResult = CoronaTestResult.RAT_REDEEMED,
        testedAt = Instant.EPOCH,
        isDccConsentGiven = false,
        isDccSupportedByPoc = false
    )

    private val anotherRAT = RACoronaTest(
        identifier = "rat-identifier-another",
        lastUpdatedAt = Instant.EPOCH,
        registeredAt = Instant.EPOCH,
        registrationToken = "token-another",
        testResult = CoronaTestResult.RAT_REDEEMED,
        testedAt = Instant.EPOCH,
        isDccConsentGiven = false,
        isDccSupportedByPoc = false
    )

    private val recycledPCR = PCRCoronaTest(
        identifier = "pcr-identifier",
        lastUpdatedAt = Instant.EPOCH,
        registeredAt = Instant.EPOCH,
        registrationToken = "token",
        testResult = CoronaTestResult.PCR_NEGATIVE,
        isDccConsentGiven = true
    )

    private val anotherPCR = PCRCoronaTest(
        identifier = "pcr-identifier-another",
        lastUpdatedAt = Instant.EPOCH,
        registeredAt = Instant.EPOCH,
        registrationToken = "token-another",
        testResult = CoronaTestResult.PCR_NEGATIVE,
        isDccConsentGiven = true
    )

    private val recCert: RecoveryCertificate = mockk {
        every { containerId } returns RecoveryCertificateContainerId("recCert")
        every { recycledAt } returns Instant.EPOCH
    }

    private val testCert: TestCertificate = mockk {
        every { containerId } returns TestCertificateContainerId("testCert")
        every { recycledAt } returns Instant.EPOCH
    }

    private val vaccCert: VaccinationCertificate = mockk {
        every { containerId } returns VaccinationCertificateContainerId("vaccCert")
        every { recycledAt } returns Instant.EPOCH
    }

    private val cwaCert: CwaCovidCertificate = mockk {
        every { containerId } returns mockk()
        every { recycledAt } returns Instant.EPOCH
    }

    private val instance: RecyclerBinOverviewViewModel
        get() = RecyclerBinOverviewViewModel(
            dispatcherProvider = TestDispatcherProvider(),
            recycledCertificatesProvider = recycledCertificatesProvider,
            recycledCoronaTestsProvider = recycledCoronaTestsProvider,
            submissionRepository = submissionRepository
        )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { recycledCertificatesProvider.recycledCertificates } returns
            flowOf(setOf(recCert, testCert, vaccCert))

        every { recycledCoronaTestsProvider.tests } returns flowOf(emptySet())
        coEvery { recycledCoronaTestsProvider.restoreCoronaTest(any()) } just Runs
    }

    @Test
    fun `No recycled items - empty list`() {
        every { recycledCertificatesProvider.recycledCertificates } returns flowOf(emptySet())
        instance.listItems.getOrAwaitValue() shouldBe emptyList()
    }

    @Test
    fun `Creates list with sub header and cert items and removes unknown types`() {
        every { recycledCertificatesProvider.recycledCertificates } returns
            flowOf(setOf(recCert, testCert, vaccCert, cwaCert))

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
                events.getOrAwaitValue() shouldBe RecyclerBinEvent.RemoveCertificate(cert, pos)
                it.onRestore(cert)
                events.getOrAwaitValue() shouldBe RecyclerBinEvent.ConfirmRestoreCertificate(cert)
            }

            (items[2] as TestCertificateCard.Item).also {
                val cert = testCert
                val pos = 2
                it.certificate shouldBe cert
                it.onRemove(cert, pos)
                events.getOrAwaitValue() shouldBe RecyclerBinEvent.RemoveCertificate(cert, pos)
                it.onRestore(cert)
                events.getOrAwaitValue() shouldBe RecyclerBinEvent.ConfirmRestoreCertificate(cert)
            }

            (items[3] as VaccinationCertificateCard.Item).also {
                val cert = vaccCert
                val pos = 3
                it.certificate shouldBe cert
                it.onRemove(cert, pos)
                events.getOrAwaitValue() shouldBe RecyclerBinEvent.RemoveCertificate(cert, pos)
                it.onRestore(cert)
                events.getOrAwaitValue() shouldBe RecyclerBinEvent.ConfirmRestoreCertificate(cert)
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
            with(recycledCertificatesProvider) {
                recycledCertificates
                deleteAllCertificate(containerIds)
            }
        }
    }

    @Test
    fun `onRemoveItem triggers deletion`() {
        val containerId = testCert.containerId
        instance.onRemoveCertificate(testCert)

        coVerify {
            recycledCertificatesProvider.deleteCertificate(containerId)
        }
    }

    @Test
    fun `onRestoreConfirmation triggers restoration`() {
        val containerId = testCert.containerId
        instance.onRestoreCertificateConfirmation(testCert)

        coVerify {
            recycledCertificatesProvider.restoreCertificate(containerId)
        }
    }

    @Test
    fun `restoreCoronaTest PCR test when another PCR is active`() {
        every { submissionRepository.testForType(CoronaTest.Type.PCR) } returns flowOf(anotherPCR)
        instance.apply {
            onRestoreTestConfirmation(recycledPCR)
            events.getOrAwaitValue() shouldBe
                RecyclerBinEvent.RestoreDuplicateTest(recycledPCR.toRestoreRecycledTestRequest(true))
        }
        coVerify(exactly = 0) { recycledCoronaTestsProvider.restoreCoronaTest(any()) }
    }

    @Test
    fun `restoreCoronaTest RAT test when another RAT is active`() {
        every { submissionRepository.testForType(CoronaTest.Type.RAPID_ANTIGEN) } returns flowOf(anotherRAT)

        instance.apply {
            onRestoreTestConfirmation(recycledRAT)
            events.getOrAwaitValue() shouldBe
                RecyclerBinEvent.RestoreDuplicateTest(recycledRAT.toRestoreRecycledTestRequest(true))
        }
        coVerify(exactly = 0) { recycledCoronaTestsProvider.restoreCoronaTest(any()) }
    }

    @Test
    fun `restoreCoronaTest PCR test is pending`() {
        every { submissionRepository.testForType(CoronaTest.Type.PCR) } returns flowOf(null)
        val recycledCoronaTest = recycledPCR.copy(testResult = CoronaTestResult.PCR_OR_RAT_PENDING)
        instance.apply {
            onRestoreTestConfirmation(recycledCoronaTest)
        }
        coVerify { recycledCoronaTestsProvider.restoreCoronaTest(any()) }
    }

    @Test
    fun `restoreCoronaTest RAT test is pending`() {
        every { submissionRepository.testForType(CoronaTest.Type.RAPID_ANTIGEN) } returns flowOf(null)

        val recycledCoronaTest = recycledRAT.copy(testResult = CoronaTestResult.PCR_OR_RAT_PENDING)
        instance.apply {
            onRestoreTestConfirmation(recycledCoronaTest)
        }
        coVerify { recycledCoronaTestsProvider.restoreCoronaTest(any()) }
    }

    @Test
    fun `restoreCoronaTest PCR test is not pending`() {
        every { submissionRepository.testForType(CoronaTest.Type.PCR) } returns flowOf(null)
        instance.apply {
            onRestoreTestConfirmation(recycledPCR)
        }
        coVerify { recycledCoronaTestsProvider.restoreCoronaTest(any()) }
    }

    @Test
    fun `restoreCoronaTest RAT test is not pending`() {
        every { submissionRepository.testForType(CoronaTest.Type.RAPID_ANTIGEN) } returns flowOf(null)

        instance.apply {
            onRestoreTestConfirmation(recycledRAT)
        }
        coVerify { recycledCoronaTestsProvider.restoreCoronaTest(any()) }
    }
}
