package de.rki.coronawarnapp.profile.ui.list

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.Hold
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ProfileListFragmentBinding
import de.rki.coronawarnapp.util.lists.decorations.TopBottomPaddingDecorator
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.onScroll
import de.rki.coronawarnapp.util.ui.addTitleId
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

@AndroidEntryPoint
class ProfileListFragment : Fragment(R.layout.profile_list_fragment) {

    private val viewModel: ProfileListViewModel by viewModels()
    private val binding: ProfileListFragmentBinding by viewBinding()
    private val profilesListAdapter = ProfileListAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.apply {
            adapter = profilesListAdapter
            addItemDecoration(TopBottomPaddingDecorator(topPadding = R.dimen.standard_8))
            onScroll {
                onScrollChange(it)
            }
        }
        binding.toolbar.apply {
            addTitleId(R.id.profile_list_fragment_title)
            setNavigationOnClickListener { popBackStack() }
            setOnMenuItemClickListener {
                findNavController().navigate(
                    ProfileListFragmentDirections.actionProfileListFragmentToProfileOnboardingFragment(
                        showButton = false
                    )
                )
                true
            }
        }
        binding.profileFab.setOnClickListener {
            viewModel.onCreateProfileClicked()
        }

        viewModel.profiles.observe(viewLifecycleOwner) {
            profilesListAdapter.update(it)
            binding.apply {
                recyclerView.isGone = it.isEmpty()
                profileListNoItemsGroup.isGone = it.isNotEmpty()
            }
        }

        viewModel.events.observe(viewLifecycleOwner) {
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
