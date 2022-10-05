package de.rki.coronawarnapp.ui.launcher

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.rootdetection.ui.showRootDetectionDialog
import de.rki.coronawarnapp.ui.dialog.displayDialog
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
                LauncherEvent.RestartApp -> restartApp()
                is LauncherEvent.ErrorToast -> {
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                    restartApp()
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.onResult(requestCode, resultCode)
    }

    private fun showUpdateNeededDialog() {
        displayDialog(cancelable = false) {
            setTitle(R.string.update_dialog_title)
            setMessage(R.string.update_dialog_message)
            setCancelable(false)
            setPositiveButton(R.string.update_dialog_button) { _, _ -> viewModel.requestUpdate() }
        }
    }

    private fun checkEnvSetup() {
        when (val parameter = intent.getEnvironmentParameters()) {
            is LauncherParameter.Base64Environment -> viewModel.setEnvironment(parameter)
            is LauncherParameter.EnvironmentKey -> viewModel.setEnvironment(parameter)
            null -> viewModel.initialization()
        }
    }

    private fun Intent.getEnvironmentParameters(): LauncherParameter? {
        if (CWADebug.buildFlavor == CWADebug.BuildFlavor.DEVICE_FOR_TESTERS) {
            extras?.getString(ENVIRONMENT_KEY)?.let {
                Timber.d("App started with parameter $ENVIRONMENT_KEY = $it")
                return LauncherParameter.EnvironmentKey(it)
            }
            extras?.getString(ENVIRONMENT_BASE64_KEY)?.let {
                Timber.d("App started with parameter $ENVIRONMENT_BASE64_KEY = $it")
                return LauncherParameter.Base64Environment(it)
            }
        }
        return null
    }

    private fun restartApp() {
        Timber.d("App will be restarted")
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
