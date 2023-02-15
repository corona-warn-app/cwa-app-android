package de.rki.coronawarnapp.ui.main

import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R

/**
 * Activity that can be used for screenshot tests where you want to load your fragments into an AppCompatActivity
 * see [TestExtensions#launchInEmptyActivity]
 */
@AndroidEntryPoint
class FakeEmptyActivity : AppCompatActivity(R.layout.activity_fake_empty)
