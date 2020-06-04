package de.rki.coronawarnapp.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.showDialogWithStacktraceIfPreviouslyCrashed
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.util.ConnectivityHelper
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        settingsViewModel = ViewModelProviders.of(this).get(SettingsViewModel::class.java)
    }

    /**
     * Register network and bluetooth callback.
     */
    override fun onResume() {
        super.onResume()
        ConnectivityHelper.registerNetworkStatusCallback(this, callbackNetwork)
        ConnectivityHelper.registerBluetoothStatusCallback(this, callbackBluetooth)
        showDialogWithStacktraceIfPreviouslyCrashed()
        scheduleWork()
    }

    /**
     * Unregister network and bluetooth callback.
     */
    override fun onPause() {
        super.onPause()
        ConnectivityHelper.unregisterNetworkStatusCallback(this, callbackNetwork)
        ConnectivityHelper.unregisterBluetoothStatusCallback(this, callbackBluetooth)
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
