package de.rki.coronawarnapp.reyclebin.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.TestDccV1
import de.rki.coronawarnapp.covidcertificate.common.repository.RecoveryCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.common.repository.VaccinationCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.reyclebin.ui.adapter.OverviewSubHeaderItem
import de.rki.coronawarnapp.reyclebin.ui.adapter.RecoveryCertificateCard
import de.rki.coronawarnapp.reyclebin.ui.adapter.RecyclerBinItem
import de.rki.coronawarnapp.reyclebin.ui.adapter.TestCertificateCard
import de.rki.coronawarnapp.reyclebin.ui.adapter.VaccinationCertificateCard
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateUserTz
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.joda.time.Instant
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot

@RunWith(AndroidJUnit4::class)
class RecyclerBinOverviewFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: RecyclerBinOverviewViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        setupMockViewModel(
            object : RecyclerBinOverviewViewModel.Factory {
                override fun create(): RecyclerBinOverviewViewModel = viewModel
            }
        )
    }

    @Test
    @Screenshot
    fun emptyRecyclerBin() {
        every { viewModel.listItems } returns MutableLiveData()

        launchFragmentInContainer2<RecyclerBinOverviewFragment>()
        takeScreenshot<RecyclerBinOverviewFragment>("empty")
    }

    @Test
    @Screenshot
    fun fullRecyclerBin() {
        every { viewModel.listItems } returns recyclerBinItems()

        launchFragmentInContainer2<RecyclerBinOverviewFragment>()
        takeScreenshot<RecyclerBinOverviewFragment>("full")
    }

    private fun recyclerBinItems(): LiveData<List<RecyclerBinItem>> {
        return MutableLiveData(
            mutableListOf<RecyclerBinItem>().apply {
                add(OverviewSubHeaderItem)
                add(
                    TestCertificateCard.Item(
                        certificate = mockTestCertificate(),
                        onRemove = { _, _ -> },
                        onRestore = {}
                    )
                )
                add(
                    VaccinationCertificateCard.Item(
                        certificate = mockVaccinationCertificate(),
                        onRemove = { _, _ -> },
                        onRestore = {}
                    )
                )
                add(
                    RecoveryCertificateCard.Item(
                        certificate = mockRecoveryCertificate(),
                        onRemove = { _, _ -> },
                        onRestore = {}
                    )
                )
            }
        )
    }

    private fun mockTestCertificate(): TestCertificate = mockk<TestCertificate>().apply {
        every { containerId } returns TestCertificateContainerId("1")
        every { fullNameFormatted } returns "Andrea Schneider"
        every { rawCertificate } returns mockk<TestDccV1>().apply {
            every { test } returns mockk<DccV1.TestCertificateData>().apply {
                every { testType } returns "LP6464-4"
            }
        }
        every { sampleCollectedAt } returns Instant.parse("2021-06-30T11:35:00.000Z")
    }

    private fun mockVaccinationCertificate(): VaccinationCertificate =
        mockk<VaccinationCertificate>().apply {
            every { containerId } returns VaccinationCertificateContainerId("2")
            val localDate = Instant.parse("2021-06-01T11:35:00.000Z").toLocalDateUserTz()
            every { fullNameFormatted } returns "Mia Schneider"
            every { doseNumber } returns 1
            every { totalSeriesOfDoses } returns 2
            every { vaccinatedOn } returns localDate
        }

    private fun mockRecoveryCertificate(): RecoveryCertificate =
        mockk<RecoveryCertificate>().apply {
            every { containerId } returns RecoveryCertificateContainerId("3")
            every { fullNameFormatted } returns "Thomas Schneider"
            every { validUntil } returns Instant.parse("2021-11-23T11:35:00.000Z").toLocalDateUserTz()
        }
}

@Module
abstract class RecyclerBinOverviewFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun recyclerBinOverviewFragment(): RecyclerBinOverviewFragment
}
