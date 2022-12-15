package de.rki.coronawarnapp.ui.main.home

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.Menu
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import de.rki.coronawarnapp.NavGraphDirections
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.databinding.HomeFragmentLayoutBinding
import de.rki.coronawarnapp.reyclebin.ui.dialog.recycleTestDialog
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.ExternalActionHelper.openUrl
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.decorations.TopBottomPaddingDecorator
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.ui.addMenuId
import de.rki.coronawarnapp.util.ui.findNestedGraph
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.setItemContentDescription
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs

/**
 * After the user has finished the onboarding this fragment will be the heart of the application.
 * Three ViewModels are needed that this fragment shows all relevant information to the user.
 * Also the Menu is set here.
 */
class HomeFragment : Fragment(R.layout.home_fragment_layout), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val viewModel by cwaViewModels<HomeFragmentViewModel>(
        ownerProducer = { requireActivity().viewModelStore },
        factoryProducer = { viewModelFactory }
    )

    private val binding by viewBinding<HomeFragmentLayoutBinding>()
    private val homeAdapter = HomeAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding.toolbar) {
            addMenuId(R.id.home_fragment_menu_id)
            setupMenuIcons(menu)
            setupDebugMenu(menu)
            setupMenuItemClickListener()
            menu.setItemContentDescription(requireContext())
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = DefaultItemAnimator()
            addItemDecoration(TopBottomPaddingDecorator(topPadding = R.dimen.spacing_tiny))
            adapter = homeAdapter
        }

        binding.mainTracing.setOnClickListener {
            resetTransitions()
            findNavController().navigate(HomeFragmentDirections.actionMainFragmentToSettingsTracingFragment())
        }

        binding.mainTracingAppBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            try {
                if (abs(verticalOffset) >= appBarLayout.totalScrollRange) {
                    binding.toolbar.setBackgroundResource(R.drawable.top_app_bar_shape)
                } else {
                    binding.toolbar.setBackgroundResource(R.color.colorTopBarBackground)
                }
            } catch (e: Exception) {
                Timber.e(e, "Listener has been triggered after onDestroyView()")
            }
        }

        viewModel.showPopUps()
        viewModel.events.observe2(this) { event -> navigate(event) }
        viewModel.homeItems.observe2(this) { homeAdapter.update(it) }
        viewModel.errorEvent.observe2(this) { displayDialog { setError(it) } }
        viewModel.tracingHeaderState.observe2(this) { binding.tracingHeader = it }
        viewModel.showIncorrectDeviceTimeDialog.observe2(this) { showDialog ->
            if (showDialog) displayDialog {
                title(R.string.device_time_incorrect_dialog_headline)
                message(R.string.device_time_incorrect_dialog_body)
                positiveButton(R.string.device_time_incorrect_dialog_button_confirm) {
                    viewModel.userHasAcknowledgedIncorrectDeviceTime()
                }
            }
        }
        viewModel.coronaTestErrors.observe2(this) { tests ->
            tests.forEach { test ->
                displayDialog {
                    val testName = when (test.type) {
                        BaseCoronaTest.Type.PCR -> R.string.ag_homescreen_card_pcr_title
                        BaseCoronaTest.Type.RAPID_ANTIGEN -> R.string.ag_homescreen_card_rapidtest_title
                    }
                    title(getString(testName) + " " + getString(R.string.errors_generic_headline_short))
                    setError(test.lastError)
                }
            }
        }

        viewModel.markTestBadgesAsSeen.observe2(this) {
            Timber.tag(TAG).d("markTestBadgesAsSeen=${it.size}")
        }
        viewModel.markRiskBadgeAsSeen()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshTests()
        viewModel.initAppShortcuts()
        binding.container.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun menuIconWithText(drawable: Drawable?, title: CharSequence): CharSequence {
        if (drawable == null) return title
        return SpannableString("    $title").apply {
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            setSpan(ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun setupMenuIcons(menu: Menu) {
        listOf(
            R.id.settingsFragment,
            R.id.recyclerBinOverviewFragment,
            R.id.informationFragment,
            R.id.socialMediaMenuItem,
            R.id.mainOverviewFragment
        ).forEach { id ->
            menu.findItem(id).apply {
                title = menuIconWithText(
                    drawable = icon,
                    title = title.toString()
                )
            }
        }
    }

    private fun setupDebugMenu(menu: Menu) {
        menu.findItem(R.id.test_nav_graph).isVisible = CWADebug.isDeviceForTestersBuild
    }

    private fun MaterialToolbar.setupMenuItemClickListener() {
        setOnMenuItemClickListener { menuItem ->
            resetTransitions()

            when (menuItem.itemId) {
                R.id.socialMediaMenuItem -> {
                    openUrl(R.string.home_menu_social_media_url)
                    true
                }

                else -> menuItem.onNavDestinationSelected(findNavController())
            }
        }
    }

    private fun showRiskLevelLoweredDialog(maxEncounterAgeInDays: Long) = displayDialog {
        title(R.string.risk_lowered_dialog_headline)
        message(getString(R.string.risk_lowered_dialog_body, maxEncounterAgeInDays))
        positiveButton(R.string.risk_lowered_dialog_button_confirm) {
            viewModel.userHasAcknowledgedTheLoweredRiskLevel()
        }
        setCancelable(false)
    }

    private fun showAdditionalHighRiskLevelDialog(maxEncounterAgeInDays: Long) = displayDialog {
        title(R.string.additional_high_risk_dialog_headline)
        message(getString(R.string.additional_high_risk_dialog_body, maxEncounterAgeInDays))
        positiveButton(R.string.additional_high_risk_dialog_button_confirm) {
            viewModel.userHasAcknowledgedAdditionalHighRiskLevel()
        }
        setCancelable(false)
    }

    private fun showErrorResetDialog() = displayDialog {
        title(R.string.errors_generic_headline)
        message(R.string.errors_generic_text_catastrophic_error_recovery_via_reset)
        positiveButton(R.string.errors_generic_button_positive) { viewModel.errorResetDialogDismissed() }
        neutralButton(R.string.errors_generic_button_negative) {
            openUrl(getString(R.string.errors_generic_text_catastrophic_error_encryption_failure))
        }
        setCancelable(false)
    }

    private fun showTracingExplanationDialog(maxEncounterAgeInDays: Long) = displayDialog {
        title(R.string.risk_details_explanation_dialog_title)
        message(getString(R.string.tracing_explanation_dialog_message, maxEncounterAgeInDays))
        positiveButton(R.string.errors_generic_button_positive) {
            viewModel.tracingExplanationWasShown()
        }
        setCancelable(false)
    }

    private fun navigate(event: HomeFragmentEvents) {
        resetTransitions()
        when (event) {
            HomeFragmentEvents.ShowErrorResetDialog -> showErrorResetDialog()
            is HomeFragmentEvents.HighRiskLevelDialog -> showAdditionalHighRiskLevelDialog(event.maxEncounterAgeInDays)
            is HomeFragmentEvents.LoweredRiskLevelDialog -> showRiskLevelLoweredDialog(event.maxEncounterAgeInDays)
            HomeFragmentEvents.GoToStatisticsExplanation -> findNavController().navigate(
                HomeFragmentDirections.actionMainFragmentToStatisticsExplanationFragment()
            )

            is HomeFragmentEvents.ShowTracingExplanation -> showTracingExplanationDialog(event.maxEncounterAgeInDays)
            HomeFragmentEvents.GoToRiskDetailsFragment -> findNavController().navigate(
                HomeFragmentDirections.actionMainFragmentToRiskDetailsFragment()
            )

            HomeFragmentEvents.GoToSettingsTracingFragment -> findNavController().navigate(
                HomeFragmentDirections.actionMainFragmentToSettingsTracingFragment()
            )

            HomeFragmentEvents.GoToSubmissionDispatcher -> findNavController().navigate(
                HomeFragmentDirections.actionMainFragmentToSubmissionDispatcher()
            )

            HomeFragmentEvents.OpenFAQUrl -> openUrl(getString(R.string.main_about_link))
            is HomeFragmentEvents.ShowDeleteTestDialog -> recycleTestDialog {
                viewModel.moveTestToRecycleBinStorage(event.identifier)
            }

            is HomeFragmentEvents.OpenIncompatibleUrl -> openUrl(getString(event.url))
            is HomeFragmentEvents.OpenTraceLocationOrganizerGraph -> openPresenceTracingOrganizerGraph(event)
            is HomeFragmentEvents.GoToTestResultAvailableFragment -> findNavController().navigate(
                NavGraphDirections.actionGlobalToSubmissionTestResultAvailableFragment(event.identifier)
            )

            is HomeFragmentEvents.GoToTestResultNegativeFragment -> findNavController().navigate(
                NavGraphDirections.actionGlobalToSubmissionTestResultNegativeFragment(event.identifier)
            )

            is HomeFragmentEvents.GoToTestResultKeysSharedFragment -> findNavController().navigate(
                NavGraphDirections.actionGlobalToSubmissionTestResultKeysSharedFragment(
                    testIdentifier = event.identifier
                )
            )

            is HomeFragmentEvents.GoToTestResultPositiveFragment -> findNavController().navigate(
                NavGraphDirections.actionGlobalToSubmissionResultPositiveOtherWarningNoConsentFragment(
                    testIdentifier = event.identifier
                )
            )

            is HomeFragmentEvents.GoToTestResultPendingFragment -> findNavController().navigate(
                NavGraphDirections.actionGlobalToSubmissionTestResultPendingFragment(
                    event.identifier,
                    event.forceUpdate,
                )
            )

            HomeFragmentEvents.GoToFederalStateSelection -> findNavController().navigate(
                HomeFragmentDirections.actionMainFragmentToFederalStateSelectionFragment()
            )

            is HomeFragmentEvents.DeleteOutdatedRAT -> viewModel.deleteCoronaTest(event.identifier)
            is HomeFragmentEvents.GoToFamilyTests -> findNavController().navigate(
                HomeFragmentDirections.actionMainFragmentToFamilyTestListFragment()
            )

            is HomeFragmentEvents.OpenLinkCardUrl -> {
                val urlToOpen = when (val phoneDisplayedLanguage = Locale.getDefault().language) {
                    DE, TR -> event.url.replace(LANG, phoneDisplayedLanguage)
                    else -> event.url.replace(LANG, EN)
                }
                openUrl(urlToOpen)
            }
        }
    }

    private fun openPresenceTracingOrganizerGraph(event: HomeFragmentEvents.OpenTraceLocationOrganizerGraph) {
        if (event.qrInfoAcknowledged) {
            findNestedGraph(R.id.trace_location_organizer_nav_graph).setStartDestination(R.id.traceLocationsFragment)
        }
        findNavController().navigate(HomeFragmentDirections.actionMainFragmentToTraceLocationOrganizerNavGraph())
    }

    /**
     * Reset any assigned transitions from before such as Scanner
     */
    private fun resetTransitions() {
        exitTransition = null
        reenterTransition = null
    }

    companion object {
        val TAG = tag<HomeFragment>()
        private const val EN = "en"
        private const val DE = "de"
        private const val TR = "tr"
        private const val LANG = ":lang"
    }
}
