package de.rki.coronawarnapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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
        super.onCreate(savedInstanceState)
        updateChecker = UpdateChecker(this)
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            updateChecker.checkForUpdate()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        updateChecker.onActivityResult(requestCode, resultCode, data)
    }

    fun navigateToActivities() {
        if (LocalData.isOnboarded()) {
            startMainActivity()
        } else {
            startOnboardingActivity()
        }
    }

    private fun startOnboardingActivity() {
        val onboardingActivity = Intent(this, OnboardingActivity::class.java)
        startActivity(onboardingActivity)
        this.overridePendingTransition(0, 0)
        finish()
    }

    private fun startMainActivity() {
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        startActivity(mainActivityIntent)
        this.overridePendingTransition(0, 0)
        finish()
    }
}
