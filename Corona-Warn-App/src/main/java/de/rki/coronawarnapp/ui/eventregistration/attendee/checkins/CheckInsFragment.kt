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
import de.rki.coronawarnapp.util.isSwipeable
import de.rki.coronawarnapp.util.lists.decorations.TopBottomPaddingDecorator
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.onScroll
import de.rki.coronawarnapp.util.tryHumanReadableError
import de.rki.coronawarnapp.util.onSwipeItem
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
            onScroll { extend ->
                if (extend) binding.scanCheckinQrcodeFab.extend() else binding.scanCheckinQrcodeFab.shrink()
            }

            onSwipeItem(
                context = requireContext(),
                excludedPositions = listOf() // TODO exclude items from swiping such as Camera permission item
            ) { position, direction ->
                val checkInsItem = checkInsAdapter.data[position]
                if (checkInsItem.isSwipeable()) {
                    checkInsItem.onSwipe(position, direction)
                }
            }
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
            // TODO Remove once feature is done
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
                            verifiedTraceLocation = it.verifiedTraceLocation,
                            editCheckInId = 0,
                        )
                    )
                }

                is CheckInEvent.ConfirmSwipeItem -> {
                    showRemovalConfirmation(it.checkIn, it.position)
                }
                is CheckInEvent.ConfirmRemoveItem -> {
                    showRemovalConfirmation(it.checkIn, null)
                }
                is CheckInEvent.ConfirmRemoveAll -> {
                    showRemovalConfirmation(null, null)
                }
                is CheckInEvent.EditCheckIn -> {
                    doNavigate(
                        CheckInsFragmentDirections.actionCheckInsFragmentToConfirmCheckInFragment(
                            verifiedTraceLocation = null,
                            editCheckInId = it.checkInId,
                        )
                    )
                }
                is CheckInEvent.ShowInformation -> {
                    Toast.makeText(requireContext(), "TODO ¯\\_(ツ)_/¯", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.errorEvent.observe2(this) {
            val errorForHumans = it.tryHumanReadableError(requireContext())
            Toast.makeText(requireContext(), errorForHumans.description, Toast.LENGTH_LONG).show()
        }
    }

    private fun showRemovalConfirmation(checkIn: CheckIn?, position: Int?) =
        AlertDialog.Builder(requireContext()).apply {
            setTitle(
                if (checkIn == null) R.string.trace_location_checkins_remove_all_title
                else R.string.trace_location_checkins_remove_single_title
            )
            setMessage(R.string.trace_location_checkins_remove_message)
            setPositiveButton(R.string.generic_action_remove) { _, _ ->
                viewModel.onRemoveCheckInConfirmed(checkIn)
            }
            setNegativeButton(R.string.generic_action_abort) { _, _ ->
                position?.let {
                    checkInsAdapter.notifyItemChanged(
                        position
                    )
                }
            }
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
            "HTTPS://E.CORONAWARN.APP/C1/BJHAUJDFMNSTKMJYGY3S2NJYHA4S2NBRG5QS2YLGMM3C2ZDDHFRTSNRSGZTGIYZWCAARQAJCBVEWGZLDOJSWC3JAKNUG64BKBVGWC2LOEBJXI4TFMV2CAMJQAA4AAQAKCJDTARICEBFRIDICXSP4QTNMBRDF7EOJ3EIJD6AWT24YDOWWXQI22KCUD7R7WARBAC7ONBRPJDB2KK6QKZLF4RE3PXU7PMON4IOZVIHCYPJGBZ27FF5S4",
            "HTTPS://E.CORONAWARN.APP/C1/BJHAUJDEMVRDGZTGMU2C2MZUGQ2C2NBWGZQS2YLCHEYC2NJQHBRDCMBRMVTDIZBTCAARQAJCBVEWGZLDOJSWC3JAKNUG64BKBVGWC2LOEBJXI4TFMV2CAMJQAA4AAQAKCJDTARICEEAJRWAYJARF3V4AS5OVBODPLPX2V3IJFMFU4O2CAKRH6HGHHWCDMJYCEBH7BO2IU2EEGRKEXBZT2DAOFIMXES5ETUT45QIWDCX64APY7C2ME",
            "HTTPS://E.CORONAWARN.APP/C1/BJHAUJDBMNQWIMLFHA3S2NZQGVTC2NDDGY3C2ODGGBTC2ZBWGQYDCZJUMRTDEN3FCAARQAJCBVEWGZLDOJSWC3JAKNUG64BKBVGWC2LOEBJXI4TFMV2CAMJQAA4AAQAKCJDTARICEEAKJM3RPYMM2VVCE2GLVK6OKY36F64FRNSI6DWYV7WW6MGESFCDNNQCEA44UHS2GEWHJYHTIJ3AJYM6BC3HEIYHY2HRMPIP7ZF62YBAUKOIY",
            "HTTPS://E.CORONAWARN.APP/C1/BJHAUJBQGZRDOMJXHEYC2NBRG44C2NBWGZRC2YRQGA4S2MJTGRRDQOJZMU4DMMRQCAARQAJCBVEWGZLDOJSWC3JAKNUG64BKBVGWC2LOEBJXI4TFMV2CAMJQAA4AAQAKCJDTARICEB365TX5SEWICC3JUOAZCQX5YUK2LZZA7RGRTNBXTSEBXTD2766CAARBADXEYUJHQSE7QRQOIPEMSSPLCVC5D4I3FOBDRX64NASE47XKKK5EY",
            "HTTPS://E.CORONAWARN.APP/C1/BJHAUJDGGE4WKMTEMQZC2OJRMUYC2NBQGNTC2OJZMZRC2MTEG4ZWGMJTGA3GEOBTCAARQAJCBVEWGZLDOJSWC3JAKNUG64BKBVGWC2LOEBJXI4TFMV2CAMJQAA4AAQAKCJDTARICEEANT4HDNB7V5DWCKKUV22YQ7NYOBCTOZ2QUFBOUDZS6V5J2VRVLVSICEBU2YHAEBPQSLWTR75VFC6OEFIS22V6KU4NRDYZHTIBMHS4FDADG6",
        )
    }
}
