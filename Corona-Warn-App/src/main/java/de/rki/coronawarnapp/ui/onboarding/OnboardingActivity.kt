package de.rki.coronawarnapp.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleObserver
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.showDialogWithStacktraceIfPreviouslyCrashed

/**
 * This activity holds all the onboarding fragments and isn't used after a successful onboarding flow.
 *
 * @see LocalData
 */
class OnboardingActivity : AppCompatActivity(), LifecycleObserver {
    companion object {
        private val TAG: String? = OnboardingActivity::class.simpleName
    }

    private val FragmentManager.currentNavigationFragment: Fragment?
        get() = primaryNavigationFragment?.childFragmentManager?.fragments?.first()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Delegate the onActivityResult to the permission page
        // to get the result of the permission dialog
        // permissionPage.onActivityResult(requestCode, resultCode, data)
        supportFragmentManager.currentNavigationFragment?.onActivityResult(
            requestCode,
            resultCode,
            data
        )
    }

    override fun onResume() {
        super.onResume()
        showDialogWithStacktraceIfPreviouslyCrashed()
    }

    fun completeOnboarding() {
        LocalData.isOnboarded(true)
        LocalData.onboardingCompletedTimestamp(System.currentTimeMillis())
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    fun goBack() {
        onBackPressed()
    }
}
