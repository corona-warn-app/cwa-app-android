package de.rki.coronawarnapp.test.crash

import android.os.Bundle
import android.view.View
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSettingsCrashReportDetailsBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class SettingsCrashReportDetailsFragment :
    Fragment(R.layout.fragment_settings_crash_report_details), AutoInject {

    companion object {
        private val TAG = SettingsCrashReportDetailsFragment::class.java.simpleName
    }

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: SettingsCrashReportViewModel by cwaViewModels(
        ownerProducer = { requireActivity().viewModelStore },
        factoryProducer = { viewModelFactory }
    )
    private val fragmentSettingsCrashReportDetailsBinding: FragmentSettingsCrashReportDetailsBinding by viewBindingLazy()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fragmentSettingsCrashReportDetailsBinding.textViewCrashReportDetails.text =
            "No crash report was selected"

        vm.selectedCrashReport.observe2(this) {
            fragmentSettingsCrashReportDetailsBinding.textViewCrashReportDetails.text =
                "Selected crash report ${it.id} \n" +
                    " # appeared at: ${it.createdAt} \n\n" +
                    " # Device: ${it.deviceInfo} \n" +
                    " # Android Version ${it.androidVersion} \n" +
                    " # Android API-Level ${it.apiLevel} \n\n" +
                    " # AppVersion: ${it.appVersionName} \n" +
                    " # AppVercionCode ${it.appVersionCode} \n" +
                    " # C-Hash ${it.shortID} \n\n\n" +
                    " ${it.stackTrace}\n\n" +
                    " # Corresponding Log: \n\n ${it.logHistory}"
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentSettingsCrashReportDetailsBinding.buttonCrashReportShare.setOnClickListener { shareCrashReport() }
    }

    private fun shareCrashReport() {
        activity?.let { activity ->
            vm.selectedCrashReport.value?.let { crashReport ->
                val shareIntent = ShareCompat.IntentBuilder
                    .from(activity)
                    .setType("text/plain")
                    .setText(fragmentSettingsCrashReportDetailsBinding.textViewCrashReportDetails.text)
                    .createChooserIntent()

                if (shareIntent.resolveActivity(activity.packageManager) != null) {
                    startActivity(shareIntent)
                }
            }
        }
    }
}
