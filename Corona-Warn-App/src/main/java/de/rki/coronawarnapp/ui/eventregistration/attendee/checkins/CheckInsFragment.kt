package de.rki.coronawarnapp.ui.eventregistration.attendee.checkins

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DefaultItemAnimator
import com.google.android.material.transition.Hold
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationAttendeeCheckinsFragmentBinding
import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.decorations.TopBottomPaddingDecorator
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class CheckInsFragment : Fragment(R.layout.trace_location_attendee_checkins_fragment), AutoInject {

    private val navArgs by navArgs<CheckInsFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: CheckInsViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, savedState ->
            factory as CheckInsViewModel.Factory
            factory.create(savedState = savedState, deepLink = navArgs.uri)
        }
    )
    private val binding: TraceLocationAttendeeCheckinsFragmentBinding by viewBindingLazy()
    private val checkInsAdapter = CheckInsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = Hold()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu(binding.toolbar)

        binding.checkInsList.apply {
            adapter = checkInsAdapter
            addItemDecoration(TopBottomPaddingDecorator(topPadding = R.dimen.spacing_tiny))
            itemAnimator = DefaultItemAnimator()
        }

        viewModel.checkins.observe2(this) {
            checkInsAdapter.update(it)
            binding.apply {
                checkInsList.isGone = it.isEmpty()
                emptyListInfoContainer.isGone = it.isNotEmpty()
            }
        }

        binding.scanCheckinQrcodeFab.apply {
            setOnClickListener {
                findNavController().navigate(
                    R.id.action_checkInsFragment_to_scanCheckInQrCodeFragment,
                    null,
                    null,
                    FragmentNavigatorExtras(this to transitionName)
                )
            }
            if (CWADebug.isDeviceForTestersBuild) {
                setOnLongClickListener {
                    findNavController().navigate(
                        createCheckInUri(DEBUG_CHECKINS.random()),
                        NavOptions.Builder().apply {
                            setLaunchSingleTop(true)
                        }.build()
                    )
                    true
                }
            }
        }

        viewModel.events.observe2(this) {
            when (it) {
                is CheckInEvent.ConfirmCheckIn -> {
                    doNavigate(
                        CheckInsFragmentDirections.actionCheckInsFragmentToConfirmCheckInFragment(
                            it.result.verifiedTraceLocation
                        )
                    )
                }
                is CheckInEvent.ConfirmRemoveItem -> {
                    showRemovalConfirmation(it.checkIn)
                }
                is CheckInEvent.ConfirmRemoveAll -> {
                    showRemovalConfirmation(null)
                }
            }
        }
    }

    private fun showRemovalConfirmation(checkIn: CheckIn?) = AlertDialog.Builder(requireContext()).apply {
        setTitle(
            if (checkIn == null) R.string.trace_location_checkins_remove_all_title
            else R.string.trace_location_checkins_remove_single_title
        )
        setMessage(R.string.trace_location_checkins_remove_message)
        setPositiveButton(R.string.generic_action_remove) { _, _ ->
            viewModel.onRemoveCheckInConfirmed(checkIn)
        }
        setNegativeButton(R.string.generic_action_abort) { _, _ -> /* NOOP */ }
    }.show()

    private fun setupMenu(toolbar: Toolbar) = toolbar.apply {
        inflateMenu(R.menu.menu_trace_location_attendee_checkins)
        setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_information -> {
                    Toast.makeText(requireContext(), "Information // TODO", Toast.LENGTH_SHORT).show()
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
        fun createCheckInUri(rootUri: String): Uri = "coronawarnapp://check-ins/$rootUri".toUri()

        @Suppress("MaxLineLength")
        private val DEBUG_CHECKINS = listOf(
            "https://e.coronawarn.app/c1/BJLAUJBTGA2TKMZTGFRS2MRTGA3C2NBTMYZS2OJXGQZC2NTEHBTGCYRVGRSTQNBYCAARQARCCFGXSICCNFZHI2DEMF4SAUDBOJ2HSKQLMF2CA3LZEBYGYYLDMUYNHB5EAE4PPB5EAFAAAESGGBCAEIDFJJ7KHRO3ZZ2SFMJSBXSUY2ZZKGOIZS27L2D6VPKTA57M6RZY3MBCARR7LXAA2BY3IGNTHNFFAJSMIXF6PP4TEB3I2C3D7P32QUZHVVER",
            "https://e.coronawarn.app/c1/BJHAUJDGMNQTQNDCGM3S2NRRMMYC2NDBG5RS2YRSMY4C2OBSGVRWCZDEGUYDMY3GCAARQAJCBVEWGZLDOJSWC3JAKNUG64BKBVGWC2LOEBJXI4TFMV2CAMJQAA4AAQAKCJDDARACEA2ZCTGOF2HH2RQU7ODZMCSUTUBBNQYM6AR4NG6FFLC6ISXWEOI5UARADO44YYH3U53ZYL6IYM5DWALXUESAJNWRGRL5KLNLS5BM54SHDDCA",
        )
    }
}
