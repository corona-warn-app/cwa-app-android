package de.rki.coronawarnapp.covidcertificate.boosterinfodetails

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentBoosterInformationDetailsBinding
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.convertToHyperlink
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class BoosterInfoDetailsFragment : Fragment(R.layout.fragment_booster_information_details), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val args by navArgs<BoosterInfoDetailsFragmentArgs>()
    private val binding: FragmentBoosterInformationDetailsBinding by viewBinding()
    private val viewModel: BoosterInfoDetailsViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as BoosterInfoDetailsViewModel.Factory
            factory.create(
                groupKey = args.groupKey,
            )
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {

            toolbar.setNavigationOnClickListener { popBackStack() }
            appBarLayout.onOffsetChange { _, subtitleAlpha ->
                headerImage.alpha = subtitleAlpha
            }
        }

        viewModel.uiState.observe2(this) {
            with(binding) {
                toolbar.title = it.titleText
                subtitle.text = it.subtitleText
                body.text = it.longText
                faq.isVisible = it.faqUrl != null
                it.faqUrl?.let { url -> faq.convertToHyperlink(url) }
            }
        }

        viewModel.shouldClose.observe2(this) {
            popBackStack()
        }
    }
}
