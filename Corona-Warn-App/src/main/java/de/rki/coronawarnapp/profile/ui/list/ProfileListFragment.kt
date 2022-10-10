package de.rki.coronawarnapp.profile.ui.list

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.Hold
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ProfileListFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.decorations.TopBottomPaddingDecorator
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.onScroll
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
    private val profilesListAdapter = ProfileListAdapter()

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
            findNavController().navigate(
                ProfileListFragmentDirections.actionProfileListFragmentToProfileOnboardingFragment(
                    showButton = false
                )
            )
            true
        }
        binding.profileFab.setOnClickListener {
            viewModel.onCreateProfileClicked()
        }

        viewModel.profiles.observe2(this) {
            profilesListAdapter.update(it)
            binding.apply {
                recyclerView.isGone = it.isEmpty()
                profileListNoItemsGroup.isGone = it.isNotEmpty()
            }
        }

        viewModel.events.observe2(this) {
            when (it) {
                ProfileListEvent.NavigateToAddProfile -> {
                    findNavController().navigate(
                        R.id.action_profileListFragment_to_profileCreateFragment,
                        null,
                        null,
                        FragmentNavigatorExtras(binding.profileFab to binding.profileFab.transitionName)
                    )
                }
                is ProfileListEvent.OpenProfile -> {
                    setupHoldTransition()
                    findNavController().navigate(
                        ProfileListFragmentDirections
                            .actionProfileListFragmentToProfileQrCodeFragment(it.id)
                    )
                }
            }
        }
    }

    private fun onScrollChange(extend: Boolean) =
        with(binding.profileFab) {
            if (extend) extend() else shrink()
        }

    private fun setupHoldTransition() {
        exitTransition = Hold()
        reenterTransition = Hold()
    }
}
