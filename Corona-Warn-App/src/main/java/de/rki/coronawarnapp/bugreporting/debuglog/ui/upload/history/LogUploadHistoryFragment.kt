package de.rki.coronawarnapp.bugreporting.debuglog.ui.upload.history

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.clearAndAddAll
import de.rki.coronawarnapp.databinding.BugreportingUploadHistoryFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class LogUploadHistoryFragment : Fragment(R.layout.bugreporting_upload_history_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: LogUploadHistoryViewModel by cwaViewModels { viewModelFactory }
    private val binding: BugreportingUploadHistoryFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val historyAdapter = HistoryItemAdapter()

        binding.uploadHistory.apply {
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            adapter = historyAdapter
        }

        vm.logUploads.observe(viewLifecycleOwner) {
            historyAdapter.apply {
                data.clearAndAddAll(it)
                notifyDataSetChanged()
            }
        }

        binding.toolbar.setNavigationOnClickListener { popBackStack() }
    }
}
