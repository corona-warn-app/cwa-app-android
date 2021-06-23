package de.rki.coronawarnapp.covidcertificate.person.ui.details

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.transition.MaterialContainerTransform
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.PersonDetailsFragmentBinding
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.decorations.TopBottomPaddingDecorator
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

// Shows the list of certificates for one person
class PersonDetailsFragment : Fragment(R.layout.person_details_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val args by navArgs<PersonDetailsFragmentArgs>()
    private val binding: PersonDetailsFragmentBinding by viewBinding()
    private val viewModel: PersonDetailsViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as PersonDetailsViewModel.Factory
            factory.create(
                personIdentifierCode = args.personIdentifierCode
            )
        }
    )
    private val personDetailsAdapter = PersonDetailsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val materialContainerTransform = MaterialContainerTransform()
        sharedElementEnterTransition = materialContainerTransform
        sharedElementReturnTransition = materialContainerTransform
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backButtonCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = navigateBack()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backButtonCallback)

        binding.apply {
            root.transitionName = args.personIdentifierCode
            toolbar.setNavigationOnClickListener { navigateBack() }
            recyclerViewCertificatesList.apply {
                adapter = personDetailsAdapter
                addItemDecoration(TopBottomPaddingDecorator(topPadding = R.dimen.spacing_tiny))
            }
            appBarLayout.onOffsetChange { titleAlpha, subtitleAlpha ->
                title.alpha = titleAlpha
                europaImage.alpha = subtitleAlpha
            }

            setToolbarOverlay()
            viewModel.uiState.observe(viewLifecycleOwner) {
                personDetailsAdapter.update(it)
            }
            viewModel.events.observe(viewLifecycleOwner) { onNavEvent(it) }
        }
    }

    private fun navigateBack() {
        doNavigate(
            PersonDetailsFragmentDirections.actionPersonDetailsFragmentToPersonOverviewFragment()
        )
    }

    private fun onNavEvent(event: PersonDetailsEvents) {
        when (event) {
            is OpenRecoveryCertificateDetails -> doNavigate(
                PersonDetailsFragmentDirections
                    .actionPersonDetailsFragmentToRecoveryCertificateDetailsFragment(event.containerId)
            )
            is OpenTestCertificateDetails -> doNavigate(
                PersonDetailsFragmentDirections
                    .actionPersonDetailsFragmentToTestCertificateDetailsFragment(event.containerId)
            )
            is OpenVaccinationCertificateDetails -> doNavigate(
                PersonDetailsFragmentDirections
                    .actionPersonDetailsFragmentToVaccinationDetailsFragment(event.containerId)
            )
            Back -> navigateBack()
        }
    }

    private fun setToolbarOverlay() {

        val deviceWidth = requireContext().resources.displayMetrics.widthPixels

        val layoutParamsRecyclerView: CoordinatorLayout.LayoutParams = binding.recyclerViewCertificatesList.layoutParams
            as (CoordinatorLayout.LayoutParams)

        val textParams = binding.title.layoutParams as (LinearLayout.LayoutParams)

        val divider = 2
        textParams.bottomMargin = (deviceWidth / divider) - 24 /* 24 is space between screen border and Card */
        binding.title.requestLayout()

        val behavior: AppBarLayout.ScrollingViewBehavior =
            layoutParamsRecyclerView.behavior as (AppBarLayout.ScrollingViewBehavior)
        behavior.overlayTop = (deviceWidth / divider) - 24
    }
}
