package de.rki.coronawarnapp.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.retention.ContactDiaryWorkScheduler
import de.rki.coronawarnapp.contactdiary.ui.onboarding.ContactDiaryOnboardingFragmentDirections
import de.rki.coronawarnapp.contactdiary.ui.overview.ContactDiaryOverviewFragmentDirections
import de.rki.coronawarnapp.databinding.ActivityMainBinding
import de.rki.coronawarnapp.deadman.DeadmanNotificationScheduler
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.ui.base.startActivitySafely
import de.rki.coronawarnapp.ui.main.home.HomeFragmentViewModel
import de.rki.coronawarnapp.ui.setupWithNavController2
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.ConnectivityHelper
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.device.PowerManagement
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.ui.findNavController
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import org.joda.time.LocalDate
import javax.inject.Inject

/**
 * This activity holds all the fragments (except onboarding) and also registers a listener for
 * connectivity and bluetooth to update the ui.
 *
 * @see ConnectivityHelper
 * @see BackgroundWorkScheduler
 */
class MainActivity : AppCompatActivity(), HasAndroidInjector {
    companion object {
        const val EXTRA_ACTION = "action"

        fun start(context: Context, action: MainActivityActions? = null) {
            val intent = Intent(context, MainActivity::class.java).apply {
                if (action != null) {
                    putExtra(EXTRA_ACTION, action.name)
                    flags = flags or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
            context.startActivity(intent)
        }

        private fun getActionFromIntent(intent: Intent): MainActivityActions? {
            val extra = intent.getStringExtra(EXTRA_ACTION)
            if (extra != null) {
                return MainActivityActions.valueOf(extra)
            }
            return null
        }
    }

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>
    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: MainActivityViewModel by cwaViewModels(
        ownerProducer = { viewModelStore },
        factoryProducer = { viewModelFactory }
    )

    private val homeFragmentVm: HomeFragmentViewModel by cwaViewModels(
        ownerProducer = { viewModelStore },
        factoryProducer = { viewModelFactory }
    )

    private val FragmentManager.currentNavigationFragment: Fragment?
        get() = primaryNavigationFragment?.childFragmentManager?.fragments?.first()

    @Inject lateinit var powerManagement: PowerManagement
    @Inject lateinit var deadmanScheduler: DeadmanNotificationScheduler
    @Inject lateinit var contactDiaryWorkScheduler: ContactDiaryWorkScheduler

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

        val navController = supportFragmentManager.findNavController(R.id.nav_host_fragment)
        binding.mainBottomNavigation.setupWithNavController2(navController) {
            vm.onBottomNavSelected()
        }
        vm.isOnboardingDone.observe(this) { isOnboardingDone ->
            startNestedGraphDestination(navController, isOnboardingDone)
        }

        if (savedInstanceState == null) {
            processExtraParameters()
        }
    }

    private fun processExtraParameters() {
        when (getActionFromIntent(intent)) {
            MainActivityActions.ADD_DIARY_ENTRY -> {
                homeFragmentVm.activateContactJournalShortcut()
            }
        }
    }

    fun goToContactJournal() {
        val navController = supportFragmentManager.findNavController(R.id.nav_host_fragment)
        findViewById<BottomNavigationView>(R.id.main_bottom_navigation).selectedItemId =
            R.id.contact_diary_nav_graph

        if (vm.isOnboardingDone.value == true) {
            val today = LocalDate().toString()
            navController.navigate(
                ContactDiaryOverviewFragmentDirections.actionContactDiaryOverviewFragmentToContactDiaryDayFragment(
                    selectedDay = today
                )
            )
        } else {
            navController.navigate(
                ContactDiaryOnboardingFragmentDirections.actionContactDiaryOnboardingFragmentToContactDiaryOnboarding(
                    showBottomNav = true,
                    goToCurrentDay = true
                )
            )
        }
    }

    private fun startNestedGraphDestination(navController: NavController, isOnboardingDone: Boolean) {
        val nestedGraph = navController.graph.findNode(R.id.contact_diary_nav_graph) as NavGraph
        nestedGraph.startDestination = if (isOnboardingDone) {
            R.id.contactDiaryOverviewFragment
        } else {
            R.id.contactDiaryOnboardingFragment
        }
    }

    /**
     * Register callbacks.
     */
    override fun onResume() {
        super.onResume()
        scheduleWork()
        vm.doBackgroundNoiseCheck()
        contactDiaryWorkScheduler.schedulePeriodic()
        if (!LocalData.isAllowedToSubmitDiagnosisKeys()) {
            deadmanScheduler.schedulePeriodic()
        }
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

    /**
     * Function is called from back buttons in fragments.
     */
    fun goBack() {
        onBackPressed()
    }

    /**
     * Scheduling for a download of keys every hour.
     */
    private fun scheduleWork() = BackgroundWorkScheduler.startWorkScheduler()
}
