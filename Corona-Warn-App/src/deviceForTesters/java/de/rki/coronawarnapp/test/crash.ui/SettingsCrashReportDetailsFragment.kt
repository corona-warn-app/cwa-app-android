package de.rki.coronawarnapp.test.crash.ui

import android.os.Bundle
import android.view.View
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSettingsCrashReportDetailsBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class SettingsCrashReportDetailsFragment :
    Fragment(R.layout.fragment_settings_crash_report_details), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: SettingsCrashReportViewModel by cwaViewModels(
        ownerProducer = { requireActivity().viewModelStore },
        factoryProducer = { viewModelFactory }
    )
    private val fragmentSettingsCrashReportDetailsBinding: FragmentSettingsCrashReportDetailsBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.selectedCrashReport.observe(viewLifecycleOwner) {
            fragmentSettingsCrashReportDetailsBinding.buttonCrashReportShare.visibility = View.VISIBLE
            fragmentSettingsCrashReportDetailsBinding.buttonCrashReportShare.setOnClickListener { shareCrashReport() }
        }

        vm.selectedCrashReportFormattedText.observe(viewLifecycleOwner) {
            fragmentSettingsCrashReportDetailsBinding.textViewCrashReportDetails.text = it
        }
    }

    private fun shareCrashReport() {
        activity?.let { activity ->
            val shareIntent = ShareCompat.IntentBuilder(activity)
                .setType("text/plain")
                .setText(fragmentSettingsCrashReportDetailsBinding.textViewCrashReportDetails.text)
                .createChooserIntent()

            if (shareIntent.resolveActivity(activity.packageManager) != null) {
                startActivity(shareIntent)
            }
        }
    }

    companion object {
        private val TAG = SettingsCrashReportDetailsFragment::class.java.simpleName
    }
}
