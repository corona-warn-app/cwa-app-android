package de.rki.coronawarnapp.datadonation.analytics.ui.input

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.AnalyticsPpaUserinfoInputFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class AnalyticsUserInputFragment : Fragment(R.layout.analytics_ppa_userinfo_input_fragment), AutoInject {

    val navArgs by navArgs<AnalyticsUserInputFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: AnalyticsUserInputViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as AnalyticsUserInputViewModel.Factory
            factory.create(navArgs.type)
        }
    )

    private val binding: AnalyticsPpaUserinfoInputFragmentBinding by viewBinding()

    @Inject lateinit var itemAdapter: UserInfoItemAdapter

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
        vm.userInfoItems.observe2(this) {
            itemAdapter.data = it
        }
        itemAdapter.onItemClickListener = {
            vm.selectUserInfoItem(it)
        }
        vm.finishEvent.observe2(this) { popBackStack() }
    }

    enum class InputType {
        AGE_GROUP,
        FEDERAL_STATE,
        DISTRICT
    }
}
