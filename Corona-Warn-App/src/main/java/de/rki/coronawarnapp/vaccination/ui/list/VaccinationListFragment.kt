package de.rki.coronawarnapp.vaccination.ui.list

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.AppBarLayout
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentVaccinationListBinding
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.ui.list.VaccinationListViewModel.Event.NavigateToVaccinationCertificateDetails
import de.rki.coronawarnapp.vaccination.ui.list.adapter.VaccinationListAdapter
import javax.inject.Inject

class VaccinationListFragment : Fragment(R.layout.fragment_vaccination_list), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val args by navArgs<VaccinationListFragmentArgs>()
    private val binding: FragmentVaccinationListBinding by viewBindingLazy()
    private val viewModel: VaccinationListViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as VaccinationListViewModel.Factory
            factory.create(
                personIdentifierCode = args.personIdentifierCode
            )
        }
    )

    private val adapter = VaccinationListAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener {
                popBackStack()
            }

            recyclerViewVaccinationList.adapter = adapter

            viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
                bindViews(uiState)
            }

            viewModel.events.observe(viewLifecycleOwner) { event ->
                when (event) {
                    is NavigateToVaccinationCertificateDetails -> doNavigate(
                        VaccinationListFragmentDirections
                            .actionVaccinationListFragmentToVaccinationDetailsFragment(event.vaccinationCertificateId)
                    )
                    is VaccinationListViewModel.Event.NavigateToVaccinationQrCodeScanScreen -> doNavigate(
                        VaccinationListFragmentDirections.actionVaccinationListFragmentToVaccinationQrCodeScanFragment()
                    )
                }
            }

            registerNewVaccinationButton.setOnClickListener {
                viewModel.onRegisterNewVaccinationClick()
            }

            refreshButton.setOnClickListener {
                Toast.makeText(requireContext(), "TODO \uD83D\uDEA7", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun FragmentVaccinationListBinding.bindViews(uiState: VaccinationListViewModel.UiState) = with(uiState) {

        val isVaccinationComplete = vaccinationStatus == VaccinatedPerson.Status.COMPLETE
        setToolbarOverlay(isVaccinationComplete)

        adapter.update(listItems)

        val background = if (isVaccinationComplete) {
            R.drawable.vaccination_compelete_gradient
        } else {
            R.drawable.vaccination_incomplete
        }

        expandedImage.setImageResource(background)

        subtitle.isVisible = isVaccinationComplete

        appBarLayout.onOffsetChange { titleAlpha, subtitleAlpha ->
            title.alpha = titleAlpha
            subtitle.alpha = subtitleAlpha
        }
    }

    private fun setToolbarOverlay(isVaccinationComplete: Boolean) {

        // Reset bottom margins to the default initially
        val titleBottomMarginDefault = 12
        val layoutParamsTitle = binding.title.layoutParams as (LinearLayout.LayoutParams)
        layoutParamsTitle.bottomMargin = titleBottomMarginDefault

        // subtitle is only visible when vaccination is complete
        val bottomTextView = if (isVaccinationComplete) binding.subtitle else binding.title

        val deviceWidth = requireContext().resources.displayMetrics.widthPixels

        val layoutParamsRecyclerView: CoordinatorLayout.LayoutParams = binding.recyclerViewVaccinationList.layoutParams
            as (CoordinatorLayout.LayoutParams)

        val textParams = bottomTextView.layoutParams as (LinearLayout.LayoutParams)

        val divider = if (isVaccinationComplete) 2 else 3
        textParams.bottomMargin = (deviceWidth / divider) - 24 /* 24 is space between screen border and Card */
        bottomTextView.requestLayout()

        val behavior: AppBarLayout.ScrollingViewBehavior =
            layoutParamsRecyclerView.behavior as (AppBarLayout.ScrollingViewBehavior)
        behavior.overlayTop = (deviceWidth / divider) - 24
    }

    companion object {
        fun navigationUri(personIdentifierCode: String) = "coronawarnapp://vaccination-list/$personIdentifierCode".toUri()
    }
}
