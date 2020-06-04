package de.rki.coronawarnapp.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.rki.coronawarnapp.exception.handler.GlobalExceptionHandlerConstants
import de.rki.coronawarnapp.http.DynamicURLs
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.onboarding.OnboardingActivity

class LauncherActivity : AppCompatActivity() {
    companion object {
        private val TAG: String? = LauncherActivity::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
     //  showDialogWithStacktraceIfPreviouslyCrashed()
        retrieveCustomURLsFromSchema(intent.data)

        if (LocalData.isOnboarded()) {
            startMainActivity()
        } else {
            startOnboardingActivity()
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

    private fun startOnboardingActivity() {
        val onboardingActivity = Intent(this, OnboardingActivity::class.java)
        mapIntentExtras(onboardingActivity)
        startActivity(onboardingActivity)
        finish()
    }

    private fun startMainActivity() {
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        mapIntentExtras(mainActivityIntent)
        startActivity(mainActivityIntent)
        finish()
    }

    /**
     * Maps the intentExtras for global exception handling to the next activity that is
     * started
     *
     * @param intentForNextActivity
     */
    private fun mapIntentExtras(intentForNextActivity: Intent) {
        intentForNextActivity.putExtra(
            GlobalExceptionHandlerConstants.APP_CRASHED,
            intent.getBooleanExtra(GlobalExceptionHandlerConstants.APP_CRASHED, false)
        )
        intentForNextActivity.putExtra(
            GlobalExceptionHandlerConstants.STACK_TRACE,
            intent.getStringExtra(GlobalExceptionHandlerConstants.STACK_TRACE)
        )
    }
}
