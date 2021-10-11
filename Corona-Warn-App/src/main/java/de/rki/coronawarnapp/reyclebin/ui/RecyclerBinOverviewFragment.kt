package de.rki.coronawarnapp.reyclebin.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentStatisticsExplanationBinding
import de.rki.coronawarnapp.databinding.RecyclerBinOverviewFragmentBinding
import de.rki.coronawarnapp.tracing.ui.details.TracingDetailsFragmentViewModel
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class RecyclerBinOverviewFragment : Fragment(R.layout.recycler_bin_overview_fragment) {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: RecyclerBinOverviewViewModel by cwaViewModels { viewModelFactory }
    private val binding: RecyclerBinOverviewFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
