package de.rki.coronawarnapp.ui.coronatest.rat.profile.list

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.google.android.material.transition.Hold
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ProfileListFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.decorations.TopBottomPaddingDecorator
import de.rki.coronawarnapp.util.onScroll
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class ProfileListFragment : Fragment(R.layout.profile_list_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: ProfileListViewModel by cwaViewModels { viewModelFactory }
    private val binding: ProfileListFragmentBinding by viewBinding()
    private val profilesListAdapter = ProfilesListAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.apply {
            adapter = profilesListAdapter
            addItemDecoration(TopBottomPaddingDecorator(topPadding = R.dimen.spacing_tiny))
            onScroll {
                onScrollChange(it)
            }
        }
        binding.toolbar.setNavigationOnClickListener { popBackStack() }
        binding.toolbar.setOnMenuItemClickListener {
            doNavigate(
                ProfileListFragmentDirections.actionProfileListFragmentToRatProfileOnboardingFragment(
                    showButton = false
                )
            )
            true
        }
        binding.profileFab.setOnClickListener {
            viewModel.onCreateProfileClicked()
        }

        viewModel.events.observe2(this) {
            when (it) {
                ProfileListEvent.NavigateToAddProfile -> {
                    findNavController().navigate(
                        R.id.action_profileListFragment_to_ratProfileCreateFragment,
                        null,
                        null,
                        FragmentNavigatorExtras(binding.profileFab to binding.profileFab.transitionName)
                    )
                }
            }
        }
    }

    private fun onScrollChange(extend: Boolean) =
        with(binding.profileFab) {
            if (extend) extend() else shrink()
        }
}
