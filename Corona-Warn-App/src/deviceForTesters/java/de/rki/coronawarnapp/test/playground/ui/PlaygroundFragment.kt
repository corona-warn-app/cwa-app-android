package de.rki.coronawarnapp.test.playground.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestPlaygroundBinding
import de.rki.coronawarnapp.datadonation.analytics.ui.input.AnalyticsUserInputFragment
import de.rki.coronawarnapp.datadonation.analytics.ui.input.AnalyticsUserInputFragmentArgs
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

@SuppressLint("SetTextI18n")
class PlaygroundFragment : Fragment(R.layout.fragment_test_playground), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val vm: PlaygroundViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentTestPlaygroundBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            dataDonationUserinfoAgegroup.setOnClickListener {
                findNavController().navigate(
                    R.id.analyticsUserInputFragment,
                    AnalyticsUserInputFragmentArgs(AnalyticsUserInputFragment.InputType.AGE_GROUP).toBundle()
                )
            }
            dataDonationUserinfoFederalstate.setOnClickListener {
                findNavController().navigate(
                    R.id.analyticsUserInputFragment,
                    AnalyticsUserInputFragmentArgs(AnalyticsUserInputFragment.InputType.FEDERAL_STATE).toBundle()
                )
            }
            dataDonationUserinfoDistrict.setOnClickListener {
                findNavController().navigate(
                    R.id.analyticsUserInputFragment,
                    AnalyticsUserInputFragmentArgs(AnalyticsUserInputFragment.InputType.DISTRICT).toBundle()
                )
            }
        }
    }

    companion object {
        val TAG: String = PlaygroundFragment::class.simpleName!!
        val MENU_ITEM = TestMenuItem(
            title = "Playground",
            description = "Random options for not integrated features",
            targetId = R.id.playgroundFragment
        )
    }
}
