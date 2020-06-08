package de.rki.coronawarnapp.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import de.rki.coronawarnapp.http.config.DynamicURLs
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
        retrieveCustomURLsFromSchema(intent.data)
        updateChecker = UpdateChecker(this)

        lifecycleScope.launch {
            updateChecker.checkForUpdate()
        }
    }

    /**
     * Retrieves the custom server URLs for testing purposes.
     * The schema to start the application: coronawarnapp://launch?SUBMISSION_CDN_URL=<custom_url>&DOWNLOAD_CDN_URL=<custom_url>&VERIFICATION_CDN_URL=<custom_url>
     *
     * @param intentData
     */
    private fun retrieveCustomURLsFromSchema(intentData: Uri?) {
        if (intentData != null) {
            val downloadCDNUrlFromParameter = intentData.getQueryParameter("DOWNLOAD_CDN_URL")
            if (!downloadCDNUrlFromParameter.isNullOrEmpty()) {
                DynamicURLs.DOWNLOAD_CDN_URL = downloadCDNUrlFromParameter
            }

            val submissionCDNUrlFromParameter = intentData.getQueryParameter("SUBMISSION_CDN_URL")
            if (!submissionCDNUrlFromParameter.isNullOrEmpty()) {
                DynamicURLs.SUBMISSION_CDN_URL = submissionCDNUrlFromParameter
            }

            val verificationCDNUrlFromParameter =
                intentData.getQueryParameter("VERIFICATION_CDN_URL")
            if (!verificationCDNUrlFromParameter.isNullOrEmpty()) {
                DynamicURLs.VERIFICATION_CDN_URL = verificationCDNUrlFromParameter
            }
            val toast = Toast.makeText(this, "You now using custom server URLs", Toast.LENGTH_LONG)
            toast.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        updateChecker.onActivityResult(requestCode, resultCode)
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
