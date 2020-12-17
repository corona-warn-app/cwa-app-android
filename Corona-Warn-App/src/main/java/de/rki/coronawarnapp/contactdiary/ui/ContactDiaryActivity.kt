package de.rki.coronawarnapp.contactdiary.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.NavHostFragment
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.submission.ContactDiarySettings
import de.rki.coronawarnapp.util.di.AppInjector
import javax.inject.Inject

/**
 * This activity holds all the contact diary fragments
 */
class ContactDiaryActivity : AppCompatActivity(), HasAndroidInjector {
    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, ContactDiaryActivity::class.java))
        }
    }

    @Inject lateinit var settings: ContactDiarySettings
    @Inject lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>
    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    private val FragmentManager.currentNavigationFragment: Fragment?
        get() = primaryNavigationFragment?.childFragmentManager?.fragments?.first()

    override fun onCreate(savedInstanceState: Bundle?) {
        AppInjector.setup(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.contact_diary_activity)

        val navHost = supportFragmentManager.findFragmentById(R.id.contact_diary_fragment_container) as NavHostFragment?
        val navController = navHost!!.navController

        val navInflater = navController.navInflater
        val graph = navInflater.inflate(R.navigation.contact_diary_nav_graph)

        // TODO: Get data from shared prefs (ContactDiarySettings.class)
        if (false) {
            graph.startDestination = R.id.contactDiaryOnboardingFragment
        } else {
            graph.startDestination = R.id.contactDiaryOverviewFragment
        }

        navController.graph = graph
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        supportFragmentManager.currentNavigationFragment?.onActivityResult(
            requestCode,
            resultCode,
            data
        )
    }
}
