package de.rki.coronawarnapp.test.crash.ui

import android.os.Bundle
import android.view.View
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSettingsCrashReportDetailsBinding
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBinding

@AndroidEntryPoint
class SettingsCrashReportDetailsFragment : Fragment(R.layout.fragment_settings_crash_report_details) {

    private val vm: SettingsCrashReportViewModel by activityViewModels()
    private val fragmentSettingsCrashReportDetailsBinding: FragmentSettingsCrashReportDetailsBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.selectedCrashReport.observe2(this) {
            fragmentSettingsCrashReportDetailsBinding.buttonCrashReportShare.visibility = View.VISIBLE
            fragmentSettingsCrashReportDetailsBinding.buttonCrashReportShare.setOnClickListener { shareCrashReport() }
        }

        vm.selectedCrashReportFormattedText.observe2(this) {
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
