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
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.deadman.DeadmanNotificationScheduler
import de.rki.coronawarnapp.playbook.BackgroundNoise
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.ui.base.startActivitySafely
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.util.BackgroundPrioritization
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.ConnectivityHelper
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.device.PowerManagement
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * This activity holds all the fragments (except onboarding) and also registers a listener for
 * connectivity and bluetooth to update the ui.
 *
 * @see SettingsViewModel
 * @see ConnectivityHelper
 * @see BackgroundWorkScheduler
 */
class MainActivity : AppCompatActivity(), HasAndroidInjector {
    companion object {
        private val TAG: String? = MainActivity::class.simpleName

        fun start(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java))
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

    private lateinit var settingsViewModel: SettingsViewModel

    @Inject lateinit var backgroundPrioritization: BackgroundPrioritization

    @Inject lateinit var powerManagement: PowerManagement

    @Inject lateinit var deadmanScheduler: DeadmanNotificationScheduler

    /**
     * Register connection callback.
     */
    private val callbackNetwork = object : ConnectivityHelper.NetworkCallback() {
        override fun onNetworkAvailable() {
            settingsViewModel.updateConnectionEnabled(true)
        }

        override fun onNetworkUnavailable() {
            settingsViewModel.updateConnectionEnabled(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppInjector.setup(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        settingsViewModel = ViewModelProviders.of(this).get(SettingsViewModel::class.java)

        if (CWADebug.isDeviceForTestersBuild) {
            vm.showEnvironmentHint.observe2(this) {
                Toast.makeText(this, "Current environment: $it", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Register callbacks.
     */
    override fun onResume() {
        super.onResume()
        ConnectivityHelper.registerNetworkStatusCallback(this, callbackNetwork)
        settingsViewModel.updateBackgroundJobEnabled(ConnectivityHelper.autoModeEnabled(this))
        scheduleWork()
        checkShouldDisplayBackgroundWarning()
        doBackgroundNoiseCheck()
        deadmanScheduler.schedulePeriodic()
    }

    private fun doBackgroundNoiseCheck() {
        lifecycleScope.launch {
            BackgroundNoise.getInstance().foregroundScheduleCheck()
        }
    }

    private fun showEnergyOptimizedEnabledForBackground() {
        val dialog = DialogHelper.DialogInstance(
            this,
            R.string.onboarding_energy_optimized_dialog_headline,
            R.string.onboarding_energy_optimized_dialog_body,
            R.string.onboarding_energy_optimized_dialog_button_positive,
            R.string.onboarding_energy_optimized_dialog_button_negative,
            false, {
                // go to battery optimization
                startActivitySafely(powerManagement.toBatteryOptimizationSettingsIntent)
            }, {
                // keep battery optimization enabled
                showManualCheckingRequiredDialog()
            })
        DialogHelper.showDialog(dialog)
    }

    private fun checkForEnergyOptimizedEnabled() {
        if (!backgroundPrioritization.isBackgroundActivityPrioritized) {
            showEnergyOptimizedEnabledForBackground()
        }
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
            this,
            R.string.onboarding_background_fetch_dialog_headline,
            R.string.onboarding_background_fetch_dialog_body,
            R.string.onboarding_background_fetch_dialog_button_positive,
            R.string.onboarding_background_fetch_dialog_button_negative,
            false, {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", packageName, null)
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                // show battery optimization system dialog after background processing dialog
                checkForEnergyOptimizedEnabled()
            }, {
                // declined
            })
        DialogHelper.showDialog(dialog)
    }

    private fun checkShouldDisplayBackgroundWarning() {
        if (!LocalData.isBackgroundCheckDone()) {
            LocalData.isBackgroundCheckDone(true)
            if (ConnectivityHelper.isBackgroundRestricted(this)) {
                showBackgroundJobDisabledNotification()
            } else {
                checkForEnergyOptimizedEnabled()
            }
        }
    }

    /**
     * Unregister callbacks.
     */
    override fun onPause() {
        super.onPause()
        ConnectivityHelper.unregisterNetworkStatusCallback(this, callbackNetwork)
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
