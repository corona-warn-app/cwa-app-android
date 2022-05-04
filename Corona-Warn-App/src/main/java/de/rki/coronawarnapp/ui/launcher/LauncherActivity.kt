package de.rki.coronawarnapp.ui.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.rootdetection.ui.showRootDetectionDialog
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.onboarding.OnboardingActivity
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import timber.log.Timber
import javax.inject.Inject

class LauncherActivity : AppCompatActivity() {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val viewModel: LauncherActivityViewModel by cwaViewModels(
        ownerProducer = { viewModelStore },
        factoryProducer = { viewModelFactory }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        AppInjector.setup(this)
        super.onCreate(savedInstanceState)

        checkEnvSetup()

        viewModel.events.observe(this) {
            when (it) {
                LauncherEvent.GoToOnboarding -> {
                    OnboardingActivity.start(this, intent)
                    this.overridePendingTransition(0, 0)
                    finish()
                }
                LauncherEvent.GoToMainActivity -> {
                    MainActivity.start(this, intent)
                    this.overridePendingTransition(0, 0)
                    finish()
                }
                is LauncherEvent.ForceUpdate -> it.forceUpdate(this)
                LauncherEvent.ShowUpdateDialog -> showUpdateNeededDialog()
                LauncherEvent.ShowRootedDialog -> showRootDetectionDialog { viewModel.onRootedDialogDismiss() }
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.onResult(requestCode, resultCode)
    }

    private fun showUpdateNeededDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.update_dialog_title)
            .setMessage(R.string.update_dialog_message)
            .setCancelable(false)
            .setPositiveButton(R.string.update_dialog_button) { _, _ -> viewModel.requestUpdate() }
            .show()
    }

    private fun checkEnvSetup() {
        if (CWADebug.buildFlavor != CWADebug.BuildFlavor.DEVICE_FOR_TESTERS) {
            Timber.w("Tried to change currentEnvironment in PRODUCTION mode.")
            return
        }
        if (intent.extras?.containsKey(ENVIRONMENT_KEY) == true) {
            intent.extras?.getString(ENVIRONMENT_KEY)?.let {
                viewModel.setEnvironment(it)
                restartApp()
            }
        } else if (intent.extras?.containsKey(ENVIRONMENT_BASE64_KEY) == true) {
            intent.extras?.getString(ENVIRONMENT_BASE64_KEY)?.let {
                viewModel.setEnvironmentData(it)
                restartApp()
            }
        }
    }

    private fun restartApp() {
        val intent = applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
        val componentName = intent?.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        applicationContext.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }

    companion object {
        private const val ENVIRONMENT_KEY = "env"
        private const val ENVIRONMENT_BASE64_KEY = "env_base64"
    }
}
