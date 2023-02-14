package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.transition.Hold
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationAttendeeCheckinsFragmentBinding
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.qrcode.ui.QrcodeSharedViewModel
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items.CheckInsItem
import de.rki.coronawarnapp.ui.presencetracing.attendee.edit.EditCheckInFragmentArgs
import de.rki.coronawarnapp.util.list.setupSwipe
import de.rki.coronawarnapp.util.lists.decorations.TopBottomPaddingDecorator
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.tryHumanReadableError
import de.rki.coronawarnapp.util.ui.LazyString
import de.rki.coronawarnapp.util.ui.addMenuId
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import timber.log.Timber
import java.net.URLEncoder

class CheckInsFragment : Fragment(R.layout.trace_location_attendee_checkins_fragment), AutoInject {

    private val navArgs by navArgs<CheckInsFragmentArgs>()

    private val viewModel: CheckInsViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, savedState ->
            factory as CheckInsViewModel.Factory
            factory.create(
                savedState = savedState,
                deepLink = navArgs.uri,
                cleanHistory = navArgs.cleanHistory
            )
        }
    )
    private val binding: TraceLocationAttendeeCheckinsFragmentBinding by viewBinding()
    private val checkInsAdapter = CheckInsAdapter()
    private val locationViewModel by navGraphViewModels<QrcodeSharedViewModel>(R.id.nav_graph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu(binding.toolbar)
        bindRecycler()
        viewModel.checkins.observe(viewLifecycleOwner) { items ->
            updateViews(items)
            binding.toolbar.menu.findItem(R.id.menu_remove_all)?.isEnabled = items.isNotEmpty()
        }
        viewModel.events.observe(viewLifecycleOwner) { it?.let { onNavigationEvent(it) } }
        viewModel.errorEvent.observe(viewLifecycleOwner) {
            val errorForHumans = it.tryHumanReadableError(requireContext())
            Toast.makeText(requireContext(), errorForHumans.description, Toast.LENGTH_LONG).show()
        }
    }

    private fun onNavigationEvent(event: CheckInEvent) {
        when (event) {
            is CheckInEvent.ConfirmCheckIn -> {
                locationViewModel.putVerifiedTraceLocation(event.verifiedLocation)
                setupAxisTransition()
                findNavController().navigate(
                    CheckInsFragmentDirections.actionCheckInsFragmentToConfirmCheckInFragment(
                        event.verifiedLocation.locationIdHex
                    )
                )
            }

            is CheckInEvent.ConfirmCheckInWithoutHistory -> {
                locationViewModel.putVerifiedTraceLocation(event.verifiedTraceLocation)
                findNavController().navigate(
                    CheckInsFragmentDirections.actionCheckInsFragmentToConfirmCheckInFragmentCleanHistory(
                        event.verifiedTraceLocation.locationIdHex
                    )
                )
            }

            is CheckInEvent.ConfirmSwipeItem -> showRemovalConfirmation(event.checkIn, event.position)

            is CheckInEvent.ConfirmRemoveItem -> showRemovalConfirmation(event.checkIn, null)

            is CheckInEvent.ConfirmRemoveAll -> showRemovalConfirmation(null, null)

            is CheckInEvent.EditCheckIn -> {
                setupHoldTransition()
                val navigatorExtras = binding.checkInsList.layoutManager
                    ?.findViewByPosition(event.position)?.run {
                        FragmentNavigatorExtras(this to transitionName)
                    }
                findNavController().navigate(
                    R.id.action_checkInsFragment_to_editCheckInFragment,
                    EditCheckInFragmentArgs(event.checkInId).toBundle(),
                    null,
                    navigatorExtras
                )
            }

            is CheckInEvent.ShowInformation -> {
                setupAxisTransition()
                findNavController().navigate(
                    CheckInsFragmentDirections.actionCheckInsFragmentToCheckInOnboardingFragment(showBottomNav = false)
                )
            }

            is CheckInEvent.InvalidQrCode -> showInvalidQrCodeInformation(event.errorText)
        }
    }

    private fun showInvalidQrCodeInformation(lazyErrorText: LazyString) {
        val errorText = lazyErrorText.get(requireContext())
        displayDialog {
            title(R.string.trace_location_attendee_invalid_qr_code_dialog_title)
            message(getString(R.string.trace_location_attendee_invalid_qr_code_dialog_message, errorText))
            positiveButton(R.string.trace_location_attendee_invalid_qr_code_dialog_positive_button)
        }
    }

    private fun updateViews(items: List<CheckInsItem>) {
        checkInsAdapter.update(items)
        binding.apply {
            emptyListInfoContainer.isGone = items.isNotEmpty()
            checkInsList.isGone = items.isEmpty()
        }
    }

    private fun setupHoldTransition() {
        exitTransition = Hold()
        reenterTransition = Hold()
    }

    private fun setupAxisTransition() {
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    private fun bindRecycler() {
        binding.checkInsList.apply {
            adapter = checkInsAdapter
            addItemDecoration(TopBottomPaddingDecorator(topPadding = R.dimen.standard_8))
            itemAnimator = DefaultItemAnimator()
            setupSwipe(context = requireContext())
        }
    }

    private fun showRemovalConfirmation(checkIn: CheckIn?, position: Int?) = displayDialog {
        title(
            if (checkIn == null)
                R.string.trace_location_checkins_remove_all_title
            else
                R.string.trace_location_checkins_remove_single_title
        )
        message(R.string.trace_location_checkins_remove_message)
        positiveButton(R.string.generic_action_remove) { viewModel.onRemoveCheckInConfirmed(checkIn) }
        negativeButton(R.string.generic_action_abort)
        dismissAction { position?.let { checkInsAdapter.notifyItemChanged(position) } }
        setDeleteDialog(true)
    }

    private fun setupMenu(toolbar: MaterialToolbar) = toolbar.apply {
        toolbar.addMenuId(R.id.checkins_fragment_menu_id)
        setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_information -> {
                    viewModel.onInformationClicked()
                    true
                }

                R.id.menu_remove_all -> {
                    viewModel.onRemoveAllCheckIns()
                    true
                }

                else -> false
            }
        }
    }

    companion object {
        fun createDeepLink(rootUri: String, cleanHistory: Boolean = false, isOrganizerOnboarded: Boolean = false): Uri {
            val encodedUrl = try {
                URLEncoder.encode(rootUri, Charsets.UTF_8.name())
            } catch (e: Exception) {
                Timber.d(e, "URL Encoding failed url($rootUri)")
                rootUri // Pass original
            }
            return (
                "cwa://check-in-onboarding/$encodedUrl/?cleanHistory=$cleanHistory&showBottomNav=false" +
                    "&isOrganizerOnboarded=$isOrganizerOnboarded"
                ).toUri()
        }

        fun canHandle(rootUri: String): Boolean = rootUri.startsWith("https://e.coronawarn.app")
    }
}
