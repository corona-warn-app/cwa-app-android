package de.rki.coronawarnapp.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.navOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.transition.MaterialElevationScale
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import de.rki.coronawarnapp.NavGraphDirections
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
import de.rki.coronawarnapp.contactdiary.ui.overview.ContactDiaryOverviewFragmentDirections
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.databinding.ActivityMainBinding
import de.rki.coronawarnapp.datadonation.analytics.worker.DataDonationAnalyticsScheduler
import de.rki.coronawarnapp.qrcode.handler.CoronaTestQRCodeHandler
import de.rki.coronawarnapp.reyclebin.coronatest.handler.CoronaTestRestoreEvent
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.ui.base.startActivitySafely
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.ui.main.home.MainActivityEvent
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.CheckInsFragment
import de.rki.coronawarnapp.ui.setupWithNavController2
import de.rki.coronawarnapp.util.AppShortcuts
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.device.PowerManagement
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.shortcuts.AppShortcutsHelper.Companion.getShortcutExtra
import de.rki.coronawarnapp.util.ui.findNavController
import de.rki.coronawarnapp.util.ui.findNestedGraph
import de.rki.coronawarnapp.util.ui.updateCountBadge
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.abs

@Suppress("TooManyFunctions")
class MainActivity : AppCompatActivity(), HasAndroidInjector {
    companion object {
        val TAG = tag<MainActivity>()

        fun start(context: Context, launchIntent: Intent) {
            Intent(context, MainActivity::class.java).apply {
                flags = flags or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                Timber.tag(TAG).i("launchIntent:$launchIntent")
                fillIn(launchIntent, Intent.FILL_IN_DATA)
                Timber.tag(TAG).i("filledIntent:$this")
                context.startActivity(this)
            }
        }
    }

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>
    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: MainActivityViewModel by cwaViewModels(
        ownerProducer = { viewModelStore },
        factoryProducer = { viewModelFactory }
    )

    private val FragmentManager.currentNavigationFragment: Fragment?
        get() = primaryNavigationFragment?.childFragmentManager?.fragments?.first()

    private val navController by lazy { supportFragmentManager.findNavController(R.id.nav_host_fragment) }

    @Inject lateinit var powerManagement: PowerManagement
    @Inject lateinit var dataDonationAnalyticsScheduler: DataDonationAnalyticsScheduler

    /**
     * To avoid opening the scanner multiple times
     */
    private var lastFabClickTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        AppInjector.setup(this)
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (CWADebug.isDeviceForTestersBuild) {
            viewModel.showEnvironmentHint.observe(this) {
                Toast.makeText(this, "Current environment: $it", Toast.LENGTH_SHORT).show()
            }
        }

        with(binding) {
            setupWithNavController2(
                navController,
                onItemSelected = { viewModel.onBottomNavSelected() },
                onDestinationChanged = { barVisible ->
                    if (barVisible) resetCurrentFragmentTransition()
                    binding.checkToolTipVisibility(viewModel.isToolTipVisible.value == true)
                }
            )

            fabTooltip.close.setOnClickListener { viewModel.dismissTooltip() }

            scannerFab.apply {
                setShowMotionSpecResource(R.animator.fab_show)
                setHideMotionSpecResource(R.animator.fab_hide)
                setOnClickListener {
                    val time = System.currentTimeMillis()
                    if (abs(time - lastFabClickTime) >= 1000) {
                        lastFabClickTime = time
                        viewModel.openScanner()
                    }
                }
            }
        }

        viewModel.isToolTipVisible.observe(this) { showTooltip ->
            binding.checkToolTipVisibility(showTooltip)
        }

        viewModel.showBackgroundJobDisabledNotification.observe(this) {
            showBackgroundJobDisabledNotification()
        }
        viewModel.showEnergyOptimizedEnabledForBackground.observe(this) {
            showEnergyOptimizedEnabledForBackground()
        }

        viewModel.isContactDiaryOnboardingDone.observe(this) { isOnboardingDone ->
            startContactDiaryNestedGraphDestination(navController, isOnboardingDone)
        }
        viewModel.isTraceLocationOnboardingDone.observe(this) { isOnboardingDone ->
            startTraceLocationNestedGraphDestination(navController, isOnboardingDone)
        }
        viewModel.isCertificatesConsentGiven.observe(this) { isConsentGiven ->
            startCertificatesNestedGraphDestination(navController, isConsentGiven)
        }

        viewModel.activeCheckIns.observe(this) { count ->
            Timber.tag(TAG).d("activeCheckIns=$count")
            binding.mainBottomNavigation.updateCountBadge(R.id.trace_location_attendee_nav_graph, count)
        }

        viewModel.personsBadgeCount.observe(this) { count ->
            Timber.tag(TAG).d("personsBadgeCount=$count")
            binding.mainBottomNavigation.updateCountBadge(R.id.covid_certificates_graph, count)
        }

        viewModel.mainBadgeCount.observe(this) { count ->
            Timber.tag(TAG).d("mainBadgeCount=$count")
            binding.mainBottomNavigation.updateCountBadge(R.id.status_nav_graph, count)
        }

        viewModel.event.observe(this) { event ->
            when (event) {
                is MainActivityEvent.GoToCheckInsFragment -> navController.navigate(
                    CheckInsFragment.createDeepLink(event.uriString)
                )
                is MainActivityEvent.Error -> displayDialog(dialog = event.error.toErrorDialogBuilder(baseContext))
                is MainActivityEvent.OpenScanner -> navigateToScanner()
            }
        }

        viewModel.coronaTestResult.observe(this) { handleCoronaTestResult(coronaTestResult = it) }
        viewModel.coronaTestRestoreEvent.observe(this) { handCoronaTestRestoreEvent(event = it) }

        if (savedInstanceState == null) {
            processExtraParameters()
        }
    }

    private fun handleCoronaTestResult(coronaTestResult: CoronaTestQRCodeHandler.Result) = when (coronaTestResult) {
        is CoronaTestQRCodeHandler.InRecycleBin -> {
            showRestoreCoronaTestConfirmation(coronaTestResult.recycledCoronaTest)
            null
        }
        is CoronaTestQRCodeHandler.TestRegistrationSelection ->
            NavGraphDirections.actionGlobalTestRegistrationSelectionFragment(
                coronaTestQrCode = coronaTestResult.coronaTestQrCode
            )
    }?.let { navController.navigate(it) }

    private fun showRestoreCoronaTestConfirmation(recycledCoronaTest: BaseCoronaTest) =
        displayDialog(cancelable = false) {
            setTitle(R.string.recycle_bin_restore_corona_test_dialog_title)
            setCancelable(false)
            setMessage(R.string.recycle_bin_restore_corona_test_dialog_message)
            setPositiveButton(android.R.string.ok) { _, _ -> viewModel.restoreCoronaTest(recycledCoronaTest) }
        }

    private fun handCoronaTestRestoreEvent(event: CoronaTestRestoreEvent) = when (event) {
        is CoronaTestRestoreEvent.RestoreDuplicateTest -> NavGraphDirections.actionToSubmissionDeletionWarningFragment(
            event.restoreRecycledTestRequest
        )
        is CoronaTestRestoreEvent.RestoredTest -> NavGraphDirections.actionGlobalMainFragment()
    }.let { navController.navigate(it) }

    private fun ActivityMainBinding.checkToolTipVisibility(
        showTooltip: Boolean
    ) {
        fabTooltip.root.isVisible = bottomAppBar.isVisible && showTooltip
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Timber.i("onNewIntent:$intent")
        processExtraParameters()
    }

    private fun processExtraParameters() {
        when (intent.getShortcutExtra()) {
            AppShortcuts.QR_CODE_SCANNER -> goToQrCodeScanner()
            AppShortcuts.CERTIFICATES -> goToCertificates()
            AppShortcuts.CHECK_INS -> goToCheckIns()
            AppShortcuts.CONTACT_DIARY -> goToContactJournal()
            else -> Unit
        }

        navigateByIntentUri(intent)
    }

    private fun goToQrCodeScanner() {
        navController.navigate(R.id.universalScanner)
    }

    private fun goToCertificates() {
        findViewById<BottomNavigationView>(R.id.main_bottom_navigation).selectedItemId = R.id.covid_certificates_graph
        val nestedGraph = navController.findNestedGraph(R.id.covid_certificates_graph)
        val destination = if (viewModel.isCertificatesConsentGiven.value == false) {
            R.id.covidCertificateOnboardingFragment
        } else {
            R.id.personOverviewFragment
        }
        nestedGraph.setStartDestination(destination)
        navController.navigate(
            resId = destination,
            args = null,
            navOptions = navOptions {
                popUpTo(R.id.personOverviewFragment) {
                    inclusive = true
                }
            }
        )
    }

    private fun goToCheckIns() {
        findViewById<BottomNavigationView>(R.id.main_bottom_navigation).selectedItemId =
            R.id.trace_location_attendee_nav_graph
        val nestedGraph = navController.findNestedGraph(R.id.trace_location_attendee_nav_graph)
        if (viewModel.isTraceLocationOnboardingDone.value == false) {
            nestedGraph.setStartDestination(R.id.checkInOnboardingFragment)
            navController.navigate(R.id.checkInOnboardingFragment)
        } else {
            nestedGraph.setStartDestination(R.id.checkInsFragment)
            navController.navigate(
                resId = R.id.checkInsFragment,
                args = null,
                navOptions = navOptions {
                    popUpTo(R.id.checkInOnboardingFragment) {
                        inclusive = true
                    }
                }
            )
        }
    }

    private fun goToContactJournal() {
        findViewById<BottomNavigationView>(R.id.main_bottom_navigation).selectedItemId = R.id.contact_diary_nav_graph
        val nestedGraph = navController.findNestedGraph(R.id.contact_diary_nav_graph)

        if (viewModel.isContactDiaryOnboardingDone.value == true) {
            nestedGraph.setStartDestination(R.id.contactDiaryOverviewFragment)
            navController.navigate(
                ContactDiaryOverviewFragmentDirections.actionContactDiaryOverviewFragmentToContactDiaryDayFragment(
                    selectedDay = LocalDate.now().toString()
                )
            )
        } else {
            nestedGraph.setStartDestination(R.id.contactDiaryOnboardingFragment)
            navController.navigate("coronawarnapp://contact-journal/oboarding/?goToDay=true".toUri())
        }
    }

    private fun startContactDiaryNestedGraphDestination(navController: NavController, isOnboardingDone: Boolean) {
        val startDestination =
            if (isOnboardingDone) R.id.contactDiaryOverviewFragment else R.id.contactDiaryOnboardingFragment
        navController.findNestedGraph(R.id.contact_diary_nav_graph).setStartDestination(startDestination)
    }

    private fun startTraceLocationNestedGraphDestination(navController: NavController, isOnboardingDone: Boolean) {
        val startDestination = if (isOnboardingDone) R.id.checkInsFragment else R.id.checkInOnboardingFragment
        navController.findNestedGraph(R.id.trace_location_attendee_nav_graph).setStartDestination(startDestination)
    }

    private fun startCertificatesNestedGraphDestination(navController: NavController, isConsentGiven: Boolean) {
        val startDestination =
            if (isConsentGiven) R.id.personOverviewFragment else R.id.covidCertificateOnboardingFragment
        navController.findNestedGraph(R.id.covid_certificates_graph).setStartDestination(startDestination)
    }

    private fun navigateByIntentUri(intent: Intent?) {
        val uriString = intent?.data?.toString() ?: return
        Timber.i("Uri:$uriString")
        viewModel.onNavigationUri(uriString)
    }

    /**
     * Register callbacks.
     */
    override fun onResume() {
        super.onResume()
        dataDonationAnalyticsScheduler.initialize()
    }

    private fun showEnergyOptimizedEnabledForBackground() {
        displayDialog(cancelable = false) {
            setTitle(R.string.onboarding_energy_optimized_dialog_headline)
            setMessage(R.string.onboarding_energy_optimized_dialog_body)
            setPositiveButton(R.string.onboarding_energy_optimized_dialog_button_positive) { _, _ ->
                startActivitySafely(
                    powerManagement.toBatteryOptimizationSettingsIntent
                )
            }
            setNegativeButton(R.string.onboarding_energy_optimized_dialog_button_negative) { _, _ ->
                showManualCheckingRequiredDialog()
            }
        }
    }

    private fun showManualCheckingRequiredDialog() {
        displayDialog(cancelable = false) {
            setTitle(R.string.onboarding_manual_required_dialog_headline)
            setMessage(R.string.onboarding_manual_required_dialog_body)
            setPositiveButton(R.string.onboarding_manual_required_dialog_button) { _, _ -> }
        }
    }

    private fun showBackgroundJobDisabledNotification() {
        displayDialog(cancelable = false) {
            setTitle(R.string.onboarding_background_fetch_dialog_headline)
            setMessage(R.string.onboarding_background_fetch_dialog_body)
            setPositiveButton(R.string.onboarding_background_fetch_dialog_button_positive) { _, _ ->
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", packageName, null)
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                // show battery optimization system dialog after background processing dialog
                viewModel.onUserOpenedBackgroundPriorityOptions()
            }
            setNegativeButton(R.string.onboarding_background_fetch_dialog_button_negative) { _, _ -> }
        }
    }

    private fun navigateToScanner() {
        supportFragmentManager.currentNavigationFragment?.apply {
            val animDuration = resources.getInteger(R.integer.fab_scanner_transition_duration).toLong()
            exitTransition = MaterialElevationScale(false).apply { duration = animDuration }
            reenterTransition = MaterialElevationScale(true).apply { duration = animDuration }
        }
        navController.navigate(R.id.universalScanner)
    }

    private fun resetCurrentFragmentTransition() {
        supportFragmentManager.currentNavigationFragment?.apply {
            exitTransition = null
            reenterTransition = null
        }
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        supportFragmentManager.currentNavigationFragment?.onActivityResult(
            requestCode,
            resultCode,
            data
        )
    }
}
