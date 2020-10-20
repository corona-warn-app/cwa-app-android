package de.rki.coronawarnapp.ui.settings.crash

import android.os.Bundle
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
                    " # appeared at: ${it.crashedAt} \n\n" +
                    " # Device: ${it.deviceInfo} \n" +
                    " # Android Version ${it.androidVersion} \n" +
                    " # Android API-Level ${it.apiLevel} \n\n" +
                    " # AppVersion: ${it.appVersionName} \n" +
                    " # AppVercionCode ${it.appVersionCode} \n" +
                    " # C-Hash ${it.shortID} \n\n\n" +
                    " ${it.stackTrace}"
        }
    }
}
