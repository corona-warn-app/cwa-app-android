package de.rki.coronawarnapp.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleObserver
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.di.AppInjector
import javax.inject.Inject

/**
 * This activity holds all the onboarding fragments and isn't used after a successful onboarding flow.
 *
 * @see LocalData
 */
class OnboardingActivity : AppCompatActivity(), LifecycleObserver, HasAndroidInjector {
    companion object {
        private val TAG: String? = OnboardingActivity::class.simpleName

        fun start(context: Context) {
            val intent = Intent(context, OnboardingActivity::class.java)
            context.startActivity(intent)
        }
    }

    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>
    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    private val FragmentManager.currentNavigationFragment: Fragment?
        get() = primaryNavigationFragment?.childFragmentManager?.fragments?.first()

    override fun onCreate(savedInstanceState: Bundle?) {
        AppInjector.setup(this)
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

    fun completeOnboarding() {
        LocalData.isOnboarded(true)
        LocalData.onboardingCompletedTimestamp(System.currentTimeMillis())
        MainActivity.start(this)
        finish()
    }

    fun goBack() {
        onBackPressed()
    }
}
