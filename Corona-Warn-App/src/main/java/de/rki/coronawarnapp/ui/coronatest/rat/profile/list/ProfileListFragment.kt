package de.rki.coronawarnapp.ui.coronatest.rat.profile.list

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ProfileListFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.decorations.TopBottomPaddingDecorator
import de.rki.coronawarnapp.util.onScroll
import de.rki.coronawarnapp.util.ui.doNavigate
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
            when (it.itemId) {
                R.id.rat_profile_information -> doNavigate(
                    ProfileListFragmentDirections.actionProfileListFragmentToRatProfileOnboardingFragment(
                        showButton = false
                    )
                )
            }
            true
        }
        binding.profileFab.setOnClickListener {
            viewModel.onCreateProfileClicked()
        }
    }

    private fun onScrollChange(extend: Boolean) =
        with(binding.profileFab) {
            if (extend) extend() else shrink()
        }
}
