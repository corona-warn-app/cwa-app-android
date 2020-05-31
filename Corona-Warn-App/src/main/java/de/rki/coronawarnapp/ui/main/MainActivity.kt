package de.rki.coronawarnapp.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.util.ConnectivityHelper
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler

/**
 * This activity holds all the fragments (except onboarding) and also registers a listener for the connectivity to update ui regarding.
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

    private val callback = object : ConnectivityHelper.NetworkCallback() {
        override fun onNetworkAvailable() {
            settingsViewModel.updateConnectionEnabled(true)
        }

        override fun onNetworkUnavailable() {
            settingsViewModel.updateConnectionEnabled(false)
        }
    }

    init {
        scheduleWork()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        settingsViewModel = ViewModelProviders.of(this).get(SettingsViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()
        ConnectivityHelper.registerNetworkStatusCallback(this, callback)
    }

    override fun onPause() {
        ConnectivityHelper.unregisterNetworkStatusCallback(this, callback)
        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        supportFragmentManager.currentNavigationFragment?.onActivityResult(
            requestCode,
            resultCode,
            data
        )
    }

    fun goBack() {
        onBackPressed()
    }

    // Scheduling for a Download of Keys every Hour.
    // private fun scheduleDiagnosisKeysDownload() = DiagnosisKeyWorkerScheduler.startWork()
    private fun scheduleWork() = BackgroundWorkScheduler.startWorkScheduler()
}
