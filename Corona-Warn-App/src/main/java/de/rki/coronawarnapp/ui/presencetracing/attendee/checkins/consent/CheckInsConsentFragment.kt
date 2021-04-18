package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.consent

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
    private val navArgs by navArgs<CheckInsConsentFragmentArgs>()

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
                if (navArgs.preConsentGiven) {
                    // TODO show dialog from Test screen
                } else {
                    showSkipDialog()
                }
            }
            skipButton.setOnClickListener { showSkipDialog() }
            continueButton.setOnClickListener {
                viewModel.shareSelectedCheckIns()
            }
        }

        viewModel.checkIns.observe(viewLifecycleOwner) {
            adapter.update(it)
            binding.continueButton.isEnabled = it.any { item ->
                item is SelectableCheckInVH.Item && item.checkIn.isSubmissionPermitted
            }
        }
    }

    private fun showSkipDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.trace_location_attendee_consent_dialog_title)
            .setMessage(R.string.trace_location_attendee_consent_dialog_message)
            .setPositiveButton(R.string.trace_location_attendee_consent_dialog_positive_button) { _, _ ->
                viewModel.shareSelectedCheckIns()
            }
            .setNegativeButton(R.string.trace_location_attendee_consent_dialog_negative_button) { _, _ ->
                viewModel.doNotShareCheckIns()
            }
            .show()
    }
}
