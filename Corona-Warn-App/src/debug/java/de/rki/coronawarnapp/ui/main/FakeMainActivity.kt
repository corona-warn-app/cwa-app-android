package de.rki.coronawarnapp.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.rki.coronawarnapp.R

/**
 * Adds the [Fragment] required for testing and [BottomNavigationView] in a similar layout of
 * actual [MainActivity] to avoid mocking dependencies of all first level fragments in [MainActivity]
 * and test the required fragment in isolation.
 */
class FakeMainActivity : AppCompatActivity(R.layout.activity_fake_main) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fragmentName = requireNotNull(intent.getStringExtra(FRAGMENT_CLASS))
        val fragmentClass = Class.forName(fragmentName)

        val fragment = supportFragmentManager.fragmentFactory.instantiate(
            requireNotNull(fragmentClass.classLoader),
            fragmentName
        )
        supportFragmentManager
            .beginTransaction()
            .add(R.id.fake_host_fragment, fragment, fragmentName)
            .commitNow()
    }

    companion object {
        const val FRAGMENT_CLASS = "EmptyMainActivity.FRAGMENT_CLASS"
    }
}
