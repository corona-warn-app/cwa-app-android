package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.PersonOverviewFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

// Shows a list of multiple persons
class PersonOverviewFragment : Fragment(R.layout.person_overview_fragment), AutoInject {
    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: PersonOverviewViewModel by cwaViewModels { viewModelFactory }
    private val binding by viewBinding<PersonOverviewFragmentBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }
}
