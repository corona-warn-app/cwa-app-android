package de.rki.coronawarnapp.datadonation.analytics.ui.input

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.AnalyticsPpaUserinfoInputFragmentBinding
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.assistedViewModel
import javax.inject.Inject

@AndroidEntryPoint
class AnalyticsUserInputFragment : Fragment(R.layout.analytics_ppa_userinfo_input_fragment) {

    @Inject lateinit var factory: AnalyticsUserInputViewModel.Factory
    @Inject lateinit var itemAdapter: UserInfoItemAdapter

    private val navArgs by navArgs<AnalyticsUserInputFragmentArgs>()
    private val binding: AnalyticsPpaUserinfoInputFragmentBinding by viewBinding()
    private val vm: AnalyticsUserInputViewModel by assistedViewModel { factory.create(navArgs.type) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbarLabel = when (navArgs.type) {
            InputType.AGE_GROUP -> R.string.analytics_userinput_agegroup_title
            InputType.FEDERAL_STATE -> R.string.analytics_userinput_federalstate_title
            InputType.DISTRICT -> R.string.analytics_userinput_district_title
        }
        binding.toolbar.apply {
            setTitle(toolbarLabel)
            setNavigationOnClickListener { popBackStack() }
        }

        binding.inputList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = itemAdapter
        }
        vm.userInfoItems.observe(viewLifecycleOwner) {
            itemAdapter.data = it
        }
        itemAdapter.onItemClickListener = {
            vm.selectUserInfoItem(it)
        }
        vm.finishEvent.observe(viewLifecycleOwner) { popBackStack() }
    }

    enum class InputType {
        AGE_GROUP,
        FEDERAL_STATE,
        DISTRICT
    }
}
