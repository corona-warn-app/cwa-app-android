package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.consent

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.CheckInsConsentFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class CheckInsConsentFragment : Fragment(R.layout.check_ins_consent_fragment), AutoInject {

    private val binding: CheckInsConsentFragmentBinding by viewBindingLazy()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: CheckInsConsentViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, savedState ->
            factory as CheckInsConsentViewModel.Factory
            factory.create(
                savedState = savedState
            )
        }
    )

    private val adapter = CheckInsConsentAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding) {
            checkInsRecycler.adapter = adapter
            toolbar.setNavigationOnClickListener {
                showSkipDialog()
            }
        }

        viewModel.checkIns.observe(viewLifecycleOwner) {
            adapter.update(it)
        }
    }

    private fun showSkipDialog() {
        // TODO
    }
}
