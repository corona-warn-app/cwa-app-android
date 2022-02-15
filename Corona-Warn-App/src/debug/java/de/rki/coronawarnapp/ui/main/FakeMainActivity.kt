package de.rki.coronawarnapp.ui.main

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.rki.coronawarnapp.R

/**
 * Adds the [Fragment] required for testing and [BottomNavigationView] in a similar layout of
 * actual [MainActivity] to avoid mocking dependencies of all first level fragments in [MainActivity]
 * and test the required fragment in isolation.
 */
class FakeMainActivity : AppCompatActivity(R.layout.activity_fake_main)
