package de.rki.coronawarnapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.android.AndroidInjection
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.onboarding.OnboardingActivity
import de.rki.coronawarnapp.update.UpdateChecker
import kotlinx.coroutines.launch

class LauncherActivity : AppCompatActivity() {
    companion object {
        private val TAG: String? = LauncherActivity::class.simpleName
    }

    private lateinit var updateChecker: UpdateChecker

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()

        updateChecker = UpdateChecker(this)
        lifecycleScope.launch {
            updateChecker.checkForUpdate()
        }
    }

    fun navigateToActivities() {
        if (LocalData.isOnboarded()) {
            startMainActivity()
        } else {
            startOnboardingActivity()
        }
    }

    private fun startOnboardingActivity() {
        OnboardingActivity.start(this)
        this.overridePendingTransition(0, 0)
        finish()
    }

    private fun startMainActivity() {
        MainActivity.start(this)
        this.overridePendingTransition(0, 0)
        finish()
    }
}
