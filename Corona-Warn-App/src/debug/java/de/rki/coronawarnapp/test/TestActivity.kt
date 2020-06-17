package de.rki.coronawarnapp.test

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

/**
 * An activity that will be used for testing purposes, it's useful when you need to test a Fragment
 * that requires control on the host's Lifecycle.
 */
class TestActivity : AppCompatActivity() {

    fun addFragment(fragment: Fragment, tag: String? = null) {
        supportFragmentManager.beginTransaction()
            .add(fragment, tag)
            .commitNow()
    }
}
