package de.rki.coronawarnapp.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import de.rki.coronawarnapp.NavGraphDirections
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.overview.ContactDiaryOverviewFragmentDirections
import de.rki.coronawarnapp.databinding.ActivityMainBinding
import de.rki.coronawarnapp.datadonation.analytics.worker.DataDonationAnalyticsScheduler
import de.rki.coronawarnapp.ui.base.startActivitySafely
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.CheckInsFragment
import de.rki.coronawarnapp.ui.setupWithNavController2
import de.rki.coronawarnapp.ui.submission.qrcode.consent.SubmissionConsentFragment
import de.rki.coronawarnapp.util.AppShortcuts
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.device.PowerManagement
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.shortcuts.AppShortcutsHelper.Companion.getShortcutExtra
import de.rki.coronawarnapp.util.ui.findNavController
import de.rki.coronawarnapp.util.ui.findNestedGraph
import de.rki.coronawarnapp.util.ui.updateCountBadge
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import org.joda.time.LocalDate
import timber.log.Timber
import javax.inject.Inject

class MainActivity : AppCompatActivity(), HasAndroidInjector {
    companion object {
        fun start(context: Context, launchIntent: Intent) {
            Intent(context, MainActivity::class.java).apply {
                flags = flags or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                Timber.i("launchIntent:$launchIntent")
                fillIn(launchIntent, Intent.FILL_IN_DATA)
                Timber.i("filledIntent:$this")
                context.startActivity(this)
            }
        }
    }

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>
    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: MainActivityViewModel by cwaViewModels(
        ownerProducer = { viewModelStore },
        factoryProducer = { viewModelFactory }
    )

    private val FragmentManager.currentNavigationFragment: Fragment?
        get() = primaryNavigationFragment?.childFragmentManager?.fragments?.first()

    private val navController by lazy { supportFragmentManager.findNavController(R.id.nav_host_fragment) }

    @Inject lateinit var powerManagement: PowerManagement
    @Inject lateinit var dataDonationAnalyticsScheduler: DataDonationAnalyticsScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        AppInjector.setup(this)
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (CWADebug.isDeviceForTestersBuild) {
            vm.showEnvironmentHint.observe(this) {
                Toast.makeText(this, "Current environment: $it", Toast.LENGTH_SHORT).show()
            }
        }

        vm.showBackgroundJobDisabledNotification.observe(this) {
            showBackgroundJobDisabledNotification()
        }
        vm.showEnergyOptimizedEnabledForBackground.observe(this) {
            showEnergyOptimizedEnabledForBackground()
        }

        binding.mainBottomNavigation.setupWithNavController2(navController) {
            vm.onBottomNavSelected()
        }
        vm.isContactDiaryOnboardingDone.observe(this) { isOnboardingDone ->
            startContactDiaryNestedGraphDestination(navController, isOnboardingDone)
        }
        vm.isTraceLocationOnboardingDone.observe(this) { isOnboardingDone ->
            startTraceLocationNestedGraphDestination(navController, isOnboardingDone)
        }
        vm.isVaccinationConsentGiven.observe(this) { isConsentGiven ->
            startCertificatesNestedGraphDestination(navController, isConsentGiven)
        }

        vm.activeCheckIns.observe(this) { count ->
            binding.mainBottomNavigation.updateCountBadge(R.id.trace_location_attendee_nav_graph, count)
        }

        vm.newCertificates.observe(this) { count ->
            binding.mainBottomNavigation.updateCountBadge(R.id.covid_certificates_graph, count)
        }

        if (savedInstanceState == null) {
            processExtraParameters()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Timber.i("onNewIntent:$intent")
        navigateByIntentUri(intent)
    }

    private fun processExtraParameters() {
        when (intent.getShortcutExtra()) {
            AppShortcuts.CONTACT_DIARY -> goToContactJournal()
        }

        navigateByIntentUri(intent)
    }

    private fun goToContactJournal() {
        findViewById<BottomNavigationView>(R.id.main_bottom_navigation).selectedItemId =
            R.id.contact_diary_nav_graph
        val nestedGraph = navController.findNestedGraph(R.id.contact_diary_nav_graph)

        if (vm.isContactDiaryOnboardingDone.value == true) {
            nestedGraph.startDestination = R.id.contactDiaryOverviewFragment
            navController.navigate(
                ContactDiaryOverviewFragmentDirections.actionContactDiaryOverviewFragmentToContactDiaryDayFragment(
                    selectedDay = LocalDate().toString()
                )
            )
        } else {
            nestedGraph.startDestination = R.id.contactDiaryOnboardingFragment
            navController.navigate("coronawarnapp://contact-journal/oboarding/?goToDay=true".toUri())
        }
    }

    private fun startContactDiaryNestedGraphDestination(navController: NavController, isOnboardingDone: Boolean) {
        navController.findNestedGraph(R.id.contact_diary_nav_graph).startDestination = if (isOnboardingDone) {
            R.id.contactDiaryOverviewFragment
        } else {
            R.id.contactDiaryOnboardingFragment
        }
    }

    private fun startTraceLocationNestedGraphDestination(navController: NavController, isOnboardingDone: Boolean) {
        navController.findNestedGraph(R.id.trace_location_attendee_nav_graph).startDestination = if (isOnboardingDone) {
            R.id.checkInsFragment
        } else {
            R.id.checkInOnboardingFragment
        }
    }

    private fun startCertificatesNestedGraphDestination(navController: NavController, isConsentGiven: Boolean) {
        navController.findNestedGraph(R.id.covid_certificates_graph).startDestination = if (isConsentGiven) {
            R.id.personOverviewFragment
        } else {
            R.id.covidCertificateInfoFragment
        }
    }

    private fun navigateByIntentUri(intent: Intent?) {
        val uriString = intent?.data?.toString() ?: return
        Timber.i("Uri:$uriString")
        when {
            CheckInsFragment.canHandle(uriString) ->
                navController.navigate(CheckInsFragment.createDeepLink(uriString))
            SubmissionConsentFragment.canHandle(uriString) ->
                navController.navigate(NavGraphDirections.actionSubmissionConsentFragment(uriString))
        }
    }

    /**
     * Register callbacks.
     */
    override fun onResume() {
        super.onResume()
        vm.doBackgroundNoiseCheck()
        dataDonationAnalyticsScheduler.schedulePeriodic()
    }

    private fun showEnergyOptimizedEnabledForBackground() {
        val dialog = DialogHelper.DialogInstance(
            this,
            R.string.onboarding_energy_optimized_dialog_headline,
            R.string.onboarding_energy_optimized_dialog_body,
            R.string.onboarding_energy_optimized_dialog_button_positive,
            R.string.onboarding_energy_optimized_dialog_button_negative,
            false,
            {
                // go to battery optimization
                startActivitySafely(powerManagement.toBatteryOptimizationSettingsIntent)
            },
            {
                // keep battery optimization enabled
                showManualCheckingRequiredDialog()
            }
        )
        DialogHelper.showDialog(dialog)
    }

    private fun showManualCheckingRequiredDialog() {
        val dialog = DialogHelper.DialogInstance(
            this,
            R.string.onboarding_manual_required_dialog_headline,
            R.string.onboarding_manual_required_dialog_body,
            R.string.onboarding_manual_required_dialog_button,
            null,
            false
        )
        DialogHelper.showDialog(dialog)
    }

    private fun showBackgroundJobDisabledNotification() {
        val dialog = DialogHelper.DialogInstance(
            context = this,
            title = R.string.onboarding_background_fetch_dialog_headline,
            message = R.string.onboarding_background_fetch_dialog_body,
            positiveButton = R.string.onboarding_background_fetch_dialog_button_positive,
            negativeButton = R.string.onboarding_background_fetch_dialog_button_negative,
            cancelable = false,
            positiveButtonFunction = {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", packageName, null)
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                // show battery optimization system dialog after background processing dialog
                vm.onUserOpenedBackgroundPriorityOptions()
            },
            negativeButtonFunction = {
                // declined
            }
        )
        DialogHelper.showDialog(dialog)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        supportFragmentManager.currentNavigationFragment?.onActivityResult(
            requestCode,
            resultCode,
            data
        )
    }
}
