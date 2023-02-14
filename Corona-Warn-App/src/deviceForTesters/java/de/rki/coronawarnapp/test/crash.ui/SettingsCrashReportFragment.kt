package de.rki.coronawarnapp.test.crash.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.event.BugEvent
import de.rki.coronawarnapp.databinding.FragmentCrashreporterOverviewBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.ui.viewBinding
import timber.log.Timber

@AndroidEntryPoint
class SettingsCrashReportFragment : Fragment(R.layout.fragment_crashreporter_overview) {

    private val vm: SettingsCrashReportViewModel by activityViewModels()
    private val fragmentCrashreporterOverviewBinding: FragmentCrashreporterOverviewBinding by viewBinding()
    private lateinit var adapter: CrashReportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = CrashReportAdapter { crashReportClicked(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentCrashreporterOverviewBinding.list.adapter = adapter

        vm.crashReports.observe(viewLifecycleOwner) {
            adapter.updateCrashReports(it)
        }

        fragmentCrashreporterOverviewBinding.buttonClearCrashReportList.setOnClickListener {
            vm.deleteAllCrashReports()
        }

        fragmentCrashreporterOverviewBinding.buttonTestItemForCrashReport.setOnClickListener {
            vm.simulateException()
        }
    }

    private fun crashReportClicked(crashReport: BugEvent) {
        Timber.d("Clicked on crash report ${crashReport.id}")
        vm.selectCrashReport(crashReport)
        findNavController().navigate(SettingsCrashReportFragmentDirections.actionCrashReportFragmentToSettingsCrashReportDetailsFragment())
    }

    companion object {
        val TAG = SettingsCrashReportFragment::class.java.simpleName
        val MENU_ITEM = TestMenuItem(
            title = "Bug & Problem Reporter",
            description = "List of Bugs & Exceptions with share option.",
            targetId = R.id.action_testMenuFragment_to_settingsCrashReportFragment
        )
    }
}
