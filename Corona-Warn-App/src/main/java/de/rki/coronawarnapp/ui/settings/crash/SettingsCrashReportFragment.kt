package de.rki.coronawarnapp.ui.settings.crash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.crash.CrashReportEntity
import de.rki.coronawarnapp.databinding.FragmentCrashreporterOverviewBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import timber.log.Timber
import javax.inject.Inject

class SettingsCrashReportFragment : Fragment(R.layout.fragment_crashreporter_overview), AutoInject, CrashReportAdapter.ItemClickListener {

    companion object {
        private val TAG = SettingsCrashReportFragment::class.java.simpleName
    }

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: SettingsCrashReportViewModel by cwaViewModels(
        ownerProducer = { requireActivity().viewModelStore },
        factoryProducer = { viewModelFactory }
    )
    private val fragmentCrashreporterOverviewBinding: FragmentCrashreporterOverviewBinding by viewBindingLazy()
    private lateinit var adapter: CrashReportAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = CrashReportAdapter(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentCrashreporterOverviewBinding.list.adapter = adapter

        vm.crashReports.observe2(this) {
            adapter.updateCrashReports(it)
        }

        fragmentCrashreporterOverviewBinding.buttonClearCrashReportList.setOnClickListener {
            vm.deleteAllCrashReports()
        }

        fragmentCrashreporterOverviewBinding.buttonTestItemForCrashReport.setOnClickListener {
            vm.simulateExceptioin()
        }
    }

    override fun crashReportClicked(crashReport: CrashReportEntity) {
        Timber.d("Clicked on crash report ${crashReport.id}")
        vm.selectCrashReport(crashReport.id)
        doNavigate(SettingsCrashReportFragmentDirections.actionCrashReportFragmentToSettingsCrashReportDetailsFragment())
    }
}
