package de.rki.coronawarnapp.dccticketing.ui.consent.one

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelStore
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.dccticketing.core.allowlist.DccTicketingAllowListEntry
import de.rki.coronawarnapp.dccticketing.core.qrcode.DccTicketingQrCodeData
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import de.rki.coronawarnapp.dccticketing.ui.shared.DccTicketingSharedViewModel
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.Screenshot
import testhelpers.launchFragmentInContainer2
import testhelpers.takeScreenshot
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class DccTicketingConsentOneFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: DccTicketingConsentOneViewModel

    private val dccTicketingTransactionContext: DccTicketingTransactionContext = DccTicketingTransactionContext(
        initializationData = generateDccTicketingQrCodeData(),
        allowlist = setOf(generateDccTicketingAllowListEntry())
    )

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).apply {
        UiThreadStatement.runOnUiThread {
            setViewModelStore(ViewModelStore())
            setGraph(R.navigation.nav_graph)
            setCurrentDestination(R.id.dccTicketingConsentOneFragment)
        }
    }

    private val fragmentArgs = DccTicketingConsentOneFragmentArgs(
        transactionContextIdentifier = "transactionContextIdentifier"
    ).toBundle()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        setupMockViewModel(
            object : DccTicketingConsentOneViewModel.Factory {
                override fun create(
                    dccTicketingSharedViewModel: DccTicketingSharedViewModel,
                ): DccTicketingConsentOneViewModel = viewModel
            }
        )
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Screenshot
    @Test
    fun dccTicketingConsentOneScreeshots() {
        every { viewModel.uiState } returns MutableLiveData(
            DccTicketingConsentOneViewModel.UiState(
                dccTicketingTransactionContext = dccTicketingTransactionContext
            )
        )
        launchFragmentInContainer2<DccTicketingConsentOneFragment>(
            testNavHostController = navController,
            fragmentArgs = fragmentArgs
        )
        takeScreenshot<DccTicketingConsentOneFragment>()
    }

    private fun generateDccTicketingQrCodeData(): DccTicketingQrCodeData {
        return DccTicketingQrCodeData(
            protocol = "protocol",
            protocolVersion = "protocol_version",
            serviceIdentity = "service_identity",
            privacyUrl = "http://very_privacy_url.de",
            token = UUID.randomUUID().toString(),
            consent = "Yes, please",
            subject = UUID.randomUUID().toString(),
            serviceProvider = "Anbietername"
        )
    }

    private fun generateDccTicketingAllowListEntry(): DccTicketingAllowListEntry {
        return DccTicketingAllowListEntry(
            serviceProvider = "Betreiber_ValidationService",
            hostname = "http://very-host-allow-provider",
            fingerprint256 = mockk()
        )
    }
}

@Module
abstract class DccTicketingConsentOneFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun dccTicketingConsentOneFragment(): DccTicketingConsentOneFragment
}
