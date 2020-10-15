package de.rki.coronawarnapp.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import de.rki.coronawarnapp.databinding.FragmentCrashreporterOverviewBinding
import de.rki.coronawarnapp.ui.viewmodel.SettingsCrashReporterViewModel

class SettingsCrashReporterFragment : Fragment() {

    companion object {

        @JvmStatic
        fun newInstance() =
            SettingsCrashReporterFragment().apply {
                arguments = Bundle().apply {
                    // putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }

    private val settingsCrashReporterViewModel: SettingsCrashReporterViewModel by viewModels()
    private lateinit var fragmentCrashreporterOverviewBinding: FragmentCrashreporterOverviewBinding
    private lateinit var adapter: CrashReporterAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            // columnCount = it.getInt(ARG_COLUMN_COUNT)
        }

        adapter = CrashReporterAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentCrashreporterOverviewBinding =
            FragmentCrashreporterOverviewBinding.inflate(inflater)
        return fragmentCrashreporterOverviewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentCrashreporterOverviewBinding.list.adapter = adapter

        settingsCrashReporterViewModel.crashReports.observe(viewLifecycleOwner) {
            adapter.updateCrashReports(it)
        }
    }
}
