package de.rki.coronawarnapp.dccreissuance.ui.consent.acccerts

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentDccReissuanceAccCertsBinding
import de.rki.coronawarnapp.dccreissuance.ui.consent.DccReissuanceAdapter
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class DccReissuanceAccCertsFragment : Fragment(R.layout.fragment_dcc_reissuance_acc_certs), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val binding: FragmentDccReissuanceAccCertsBinding by viewBinding()
    private val args by navArgs<DccReissuanceAccCertsFragmentArgs>()
    private val viewModel: DccReissuanceAccCertsViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as DccReissuanceAccCertsViewModel.Factory
            factory.create(
                groupKey = args.groupKey,
            )
        }
    )

    private val dccReissuanceAdapter = DccReissuanceAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            toolbar.setNavigationOnClickListener { popBackStack() }
            certificateRecycler.adapter = dccReissuanceAdapter
            viewModel.certificatesLiveData.observe(viewLifecycleOwner) {
                dccReissuanceAdapter.update(it)
            }
        }
    }
}
