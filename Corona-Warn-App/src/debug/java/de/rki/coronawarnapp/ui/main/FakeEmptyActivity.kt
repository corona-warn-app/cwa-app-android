package de.rki.coronawarnapp.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.rki.coronawarnapp.R

/**
 * Activity that can be used for screenshot tests where you want to load your fragments into an AppCompatActivity
 * see [TestExtensions#launchInEmptyActivity]
 */
class FakeEmptyActivity : AppCompatActivity(R.layout.activity_fake_empty) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fragmentName = requireNotNull(intent.getStringExtra(FRAGMENT_CLASS))
        val fragmentArgs = intent.getBundleExtra(FRAGMENT_ARGUMENTS)
        val fragmentClass = Class.forName(fragmentName)

        val fragment = supportFragmentManager.fragmentFactory.instantiate(
            requireNotNull(fragmentClass.classLoader),
            fragmentName
        ).apply {
            arguments = fragmentArgs
        }

        supportFragmentManager
            .beginTransaction()
            .add(R.id.fake_host_fragment, fragment, fragmentName)
            .commitNow()
    }

    companion object {
        const val FRAGMENT_CLASS = "EmptyActivity.FRAGMENT_CLASS"
        const val FRAGMENT_ARGUMENTS = "EmptyActivity.FRAGMENT_ARGUMENTS"
    }
}
