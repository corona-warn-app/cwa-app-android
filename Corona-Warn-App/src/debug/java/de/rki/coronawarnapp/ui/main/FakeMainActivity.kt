package de.rki.coronawarnapp.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.rki.coronawarnapp.R

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
