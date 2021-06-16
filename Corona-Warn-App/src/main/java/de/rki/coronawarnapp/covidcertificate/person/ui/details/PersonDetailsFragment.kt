package de.rki.coronawarnapp.covidcertificate.person.ui.details

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DefaultItemAnimator
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.databinding.PersonDetailsFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.decorations.TopBottomPaddingDecorator
import de.rki.coronawarnapp.util.lists.diffutil.update
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            bindRecycler()
        }
        viewModel.personSpecificCertificates.observe(viewLifecycleOwner) { binding.bindViews(it.certificates)}
    }

    private fun PersonDetailsFragmentBinding.bindViews(certificates: List<CwaCovidCertificate>) {
        personDetailsAdapter.update(certificates)
    }

    private fun PersonDetailsFragmentBinding.bindRecycler() = recyclerViewCertificatesList.apply {
        adapter = personDetailsAdapter
        addItemDecoration(TopBottomPaddingDecorator(topPadding = R.dimen.spacing_tiny))
        itemAnimator = DefaultItemAnimator()

    }
}
