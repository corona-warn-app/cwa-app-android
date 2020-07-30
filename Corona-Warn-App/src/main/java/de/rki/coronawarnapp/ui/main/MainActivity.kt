package de.rki.coronawarnapp.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.util.ConnectivityHelper
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.ExternalActionHelper
import de.rki.coronawarnapp.util.PowerManagementHelper
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler

/**
 * This activity holds all the fragments (except onboarding) and also registers a listener for
 * connectivity and bluetooth to update the ui.
 *
 * @see SettingsViewModel
 * @see ConnectivityHelper
 * @see BackgroundWorkScheduler
 */
class MainActivity : AppCompatActivity() {
    companion object {
        private val TAG: String? = MainActivity::class.simpleName

        fun start(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }

    private val FragmentManager.currentNavigationFragment: Fragment?
        get() = primaryNavigationFragment?.childFragmentManager?.fragments?.first()

    private lateinit var settingsViewModel: SettingsViewModel

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

    /**
     * Register bluetooth callback.
     */
    private val callbackBluetooth = object : ConnectivityHelper.BluetoothCallback() {
        override fun onBluetoothAvailable() {
            settingsViewModel.updateBluetoothEnabled(true)
        }

        override fun onBluetoothUnavailable() {
            settingsViewModel.updateBluetoothEnabled(false)
        }
    }

    /**
     * Register location callback.
     */
    private val callbackLocation = object : ConnectivityHelper.LocationCallback() {
        override fun onLocationAvailable() {
            settingsViewModel.updateLocationEnabled(true)
        }

        override fun onLocationUnavailable() {
            settingsViewModel.updateLocationEnabled(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        settingsViewModel = ViewModelProviders.of(this).get(SettingsViewModel::class.java)
    }

    /**
     * Register network, bluetooth and data saver callback.
     */
    override fun onResume() {
        super.onResume()
        ConnectivityHelper.registerNetworkStatusCallback(this, callbackNetwork)
        ConnectivityHelper.registerBluetoothStatusCallback(this, callbackBluetooth)
        ConnectivityHelper.registerLocationStatusCallback(this, callbackLocation)
        settingsViewModel.updateBackgroundJobEnabled(ConnectivityHelper.isBackgroundJobEnabled(this))
        scheduleWork()
        checkShouldDisplayBackgroundWarning()
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
                ExternalActionHelper.disableBatteryOptimizations(this)
            }, {
                // keep battery optimization enabled
                showManualCheckingRequiredDialog()
            })
        DialogHelper.showDialog(dialog)
    }

    private fun checkForEnergyOptimizedEnabled() {
        if (!PowerManagementHelper.isIgnoringBatteryOptimizations(this)) {
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
                // declined, show additional dialog explaining manual risk calculation
                showManualCheckingRequiredDialog()
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
     * Unregister network and bluetooth callback.
     */
    override fun onPause() {
        super.onPause()
        ConnectivityHelper.unregisterNetworkStatusCallback(this, callbackNetwork)
        ConnectivityHelper.unregisterBluetoothStatusCallback(this, callbackBluetooth)
        ConnectivityHelper.unregisterLocationStatusCallback(this, callbackLocation)
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
