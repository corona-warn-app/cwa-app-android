package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DefaultItemAnimator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.Hold
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.BuildConfig
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationAttendeeCheckinsFragmentBinding
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items.CameraPermissionVH
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items.CheckInsItem
import de.rki.coronawarnapp.ui.presencetracing.attendee.edit.EditCheckInFragmentArgs
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.list.setupSwipe
import de.rki.coronawarnapp.util.lists.decorations.TopBottomPaddingDecorator
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.onScroll
import de.rki.coronawarnapp.util.tryHumanReadableError
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import timber.log.Timber
import java.net.URLEncoder
import javax.inject.Inject

class CheckInsFragment : Fragment(R.layout.trace_location_attendee_checkins_fragment), AutoInject {

    private val navArgs by navArgs<CheckInsFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu(binding.toolbar)

        bindRecycler()
        bindFAB()

        viewModel.checkins.observe2(this) { items ->
            updateViews(items)
        }

        viewModel.events.observe2(this) {
            onNavigationEvent(it)
        }

        viewModel.errorEvent.observe2(this) {
            val errorForHumans = it.tryHumanReadableError(requireContext())
            Toast.makeText(requireContext(), errorForHumans.description, Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkCameraSettings()
    }

    private fun onNavigationEvent(event: CheckInEvent?) {
        when (event) {
            is CheckInEvent.ConfirmCheckIn -> {
                setupAxisTransition()
                doNavigate(
                    CheckInsFragmentDirections.actionCheckInsFragmentToConfirmCheckInFragment(
                        verifiedTraceLocation = event.verifiedTraceLocation
                    )
                )
            }

            is CheckInEvent.ConfirmCheckInWithoutHistory -> doNavigate(
                CheckInsFragmentDirections.actionCheckInsFragmentToConfirmCheckInFragmentCleanHistory(
                    verifiedTraceLocation = event.verifiedTraceLocation
                )
            )

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
                doNavigate(CheckInsFragmentDirections.actionCheckInsFragmentToCheckInOnboardingFragment(false))
            }
            is CheckInEvent.OpenDeviceSettings -> openDeviceSettings()
        }
    }

    private fun updateViews(items: List<CheckInsItem>) {
        checkInsAdapter.update(items)
        binding.apply {
            scanCheckinQrcodeFab.isGone = items.any { it is CameraPermissionVH.Item }
            emptyListInfoContainer.isGone = items.isNotEmpty()
            checkInsList.isGone = items.isEmpty()
        }
    }

    private fun openDeviceSettings() {
        try {
            startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                )
            )
        } catch (e: ActivityNotFoundException) {
            Timber.e(e, "Could not open device settings")
            Toast.makeText(requireContext(), R.string.errors_generic_headline, Toast.LENGTH_LONG).show()
        }
    }

    private fun bindFAB() {
        binding.scanCheckinQrcodeFab.apply {
            setOnClickListener {
                setupHoldTransition()
                findNavController().navigate(
                    R.id.action_checkInsFragment_to_scanCheckInQrCodeFragment,
                    null,
                    null,
                    FragmentNavigatorExtras(this to transitionName)
                )
            }
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
            addItemDecoration(TopBottomPaddingDecorator(topPadding = R.dimen.spacing_tiny))
            itemAnimator = DefaultItemAnimator()

            with(binding.scanCheckinQrcodeFab) {
                onScroll { extend ->
                    if (extend) extend() else shrink()
                }
            }

            setupSwipe(context = requireContext())
        }
    }

    private fun showRemovalConfirmation(checkIn: CheckIn?, position: Int?) =
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(
                if (checkIn == null) R.string.trace_location_checkins_remove_all_title
                else R.string.trace_location_checkins_remove_single_title
            )
            setMessage(R.string.trace_location_checkins_remove_message)
            setPositiveButton(R.string.generic_action_remove) { _, _ ->
                viewModel.onRemoveCheckInConfirmed(checkIn)
            }
            setNegativeButton(R.string.generic_action_abort) { _, _ -> }
            setOnDismissListener {
                position?.let { checkInsAdapter.notifyItemChanged(position) }
            }
        }.show()

    private fun setupMenu(toolbar: Toolbar) = toolbar.apply {
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
                else -> onOptionsItemSelected(it)
            }
        }
    }

    companion object {
        fun createDeepLink(rootUri: String, cleanHistory: Boolean = false): Uri {
            val encodedUrl = try {
                URLEncoder.encode(rootUri, Charsets.UTF_8.name())
            } catch (e: Exception) {
                Timber.d(e, "URL Encoding failed url($rootUri)")
                rootUri // Pass original
            }
            return "coronawarnapp://check-ins/$encodedUrl/?cleanHistory=$cleanHistory".toUri()
        }

        fun canHandle(rootUri: String): Boolean = rootUri.startsWith("https://e.coronawarn.app")
    }
}
