package de.rki.coronawarnapp.ui.launcher

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.ContactDiaryActivity
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.onboarding.OnboardingActivity
import de.rki.coronawarnapp.util.AppShortcuts
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class LauncherActivity : AppCompatActivity() {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: LauncherActivityViewModel by cwaViewModels(
        ownerProducer = { viewModelStore },
        factoryProducer = { viewModelFactory }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        AppInjector.setup(this)
        super.onCreate(savedInstanceState)

        vm.events.observe(this) {
            when (it) {
                LauncherEvent.GoToOnboarding -> {
                    OnboardingActivity.start(this)
                    this.overridePendingTransition(0, 0)
                    finish()
                }
                LauncherEvent.GoToAppShortcutOrMainActivity -> {
                    when (evaluateAppShortcuts()) {
                        AppShortcuts.CONTACT_DIARY -> ContactDiaryActivity.start(this)
                        else -> MainActivity.start(this)
                    }

                    this.overridePendingTransition(0, 0)
                    finish()
                }
                is LauncherEvent.ShowUpdateDialog -> {
                    showUpdateNeededDialog(it.updateIntent)
                }
            }
        }
    }

    private fun showUpdateNeededDialog(intent: Intent) {
        AlertDialog.Builder(this)
            .setTitle(R.string.update_dialog_title)
            .setMessage(R.string.update_dialog_message)
            .setCancelable(false)
            .setPositiveButton(R.string.update_dialog_button) { _, _ ->
                ContextCompat.startActivity(this, intent, null)
            }
            .show()
    }

    private fun evaluateAppShortcuts(): AppShortcuts? {
        return if(intent.hasExtra(KEY_SHORTCUT_TYPE)) {
            val extra = intent.getStringExtra(KEY_SHORTCUT_TYPE)!!
            AppShortcuts.valueOf(extra)
        } else {
            null
        }
    }

    companion object {
        private const val KEY_SHORTCUT_TYPE = "SHORTCUT_TYPE"
    }
}
