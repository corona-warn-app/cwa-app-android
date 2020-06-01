package de.rki.coronawarnapp.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.activityViewModels
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentMainBinding
import de.rki.coronawarnapp.notification.NotificationHelper
import de.rki.coronawarnapp.timer.TimerHelper
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.ui.UiConstants
import de.rki.coronawarnapp.ui.viewmodel.SettingsViewModel
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel
import de.rki.coronawarnapp.util.OpenUrlHelper

/**
 * After the user has finished the onboarding this fragment will be the heart of the application.
 * Two VieModels are needed that this fragment shows all relevant information to the user.
 * Also the Menu is set here.
 *
 * @see tracingViewModel
 * @see settingsViewModel
 * @see PopupMenu
 */
class MainFragment : BaseFragment() {

    companion object {
        private val TAG: String? = MainFragment::class.simpleName
    }

    private val tracingViewModel: TracingViewModel by activityViewModels()
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val submissionViewModel: SubmissionViewModel by activityViewModels()
    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater)
        binding.tracingViewModel = tracingViewModel
        binding.settingsViewModel = settingsViewModel
        binding.submissionViewModel = submissionViewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
    }

    override fun onResume() {
        super.onResume()
        // refresh required data
        tracingViewModel.refreshRiskLevel()
        tracingViewModel.refreshExposureSummary()
        tracingViewModel.refreshLastTimeDiagnosisKeysFetchedDate()
        tracingViewModel.refreshIsTracingEnabled()
        tracingViewModel.refreshActiveTracingDaysInRetentionPeriod()
        settingsViewModel.refreshBackgroundJobEnabled()
        settingsViewModel.refreshBluetoothEnabled()
        TimerHelper.checkManualKeyRetrievalTimer()
        if (submissionViewModel.deviceRegistered) {
            submissionViewModel.refreshTestResult()
        }
    }

    private fun setButtonOnClickListener() {
        binding.mainTest.submissionStatusCardFetching.submissionStatusCardFetchingButton.setOnClickListener {
            doNavigate(
                MainFragmentDirections.actionMainFragmentToSubmissionResultFragment()
            )
        }
        binding.mainTest.submissionStatusCardContent.submissionStatusCardContentButton.setOnClickListener {
            doNavigate(
                MainFragmentDirections.actionMainFragmentToSubmissionResultFragment()
            )
        }
        binding.mainTestPositive.submissionStatusCardPositiveResultShowButton.setOnClickListener {
            doNavigate(
                MainFragmentDirections.actionMainFragmentToSubmissionResultFragment()
            )
        }
        binding.mainTest.submissionStatusCardUnregistered.submissionStatusCardUnregisteredButton.setOnClickListener {
            doNavigate(
                MainFragmentDirections.actionMainFragmentToSubmissionIntroFragment()
            )
        }
        binding.mainTracing.setOnClickListener {
            doNavigate(MainFragmentDirections.actionMainFragmentToSettingsTracingFragment())
        }
        binding.mainRisk.riskCard.setOnClickListener {
            doNavigate(MainFragmentDirections.actionMainFragmentToRiskDetailsFragment())
        }
        binding.mainRisk.riskCardButtonUpdate.setOnClickListener {
            tracingViewModel.refreshRiskLevel()
            tracingViewModel.refreshDiagnosisKeys()
            TimerHelper.startManualKeyRetrievalTimer()
        }
        binding.mainRisk.riskCardButtonEnableTracing.setOnClickListener {
            doNavigate(MainFragmentDirections.actionMainFragmentToSettingsTracingFragment())
        }
        binding.mainAbout.mainCard.setOnClickListener {
            OpenUrlHelper.navigate(this, UiConstants.INFORMATION_URI)
        }
        binding.mainHeaderShare.buttonIcon.setOnClickListener {
            doNavigate(MainFragmentDirections.actionMainFragmentToMainSharingFragment())
        }
        binding.mainHeaderOptionsMenu.buttonIcon.setOnClickListener {
            showPopup(it)
        }
    }

    private fun showPopup(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.inflate(R.menu.menu_main)
        popup.setOnMenuItemClickListener {
            return@setOnMenuItemClickListener when (it.itemId) {
                R.id.menu_help -> {
                    Toast.makeText(
                        requireContext(),
                        "Help Navigation isn't implemented",
                        Toast.LENGTH_LONG
                    ).show()
                    true
                }
                R.id.menu_information -> {
                    doNavigate(MainFragmentDirections.actionMainFragmentToInformationFragment())
                    true
                }
                R.id.menu_settings -> {
                    doNavigate(MainFragmentDirections.actionMainFragmentToSettingsFragment())
                    true
                }
                // todo remove only for testing
                R.id.menu_test_api -> {
                    doNavigate(MainFragmentDirections.actionMainFragmentToTestForAPIFragment())
                    true
                }
                // todo remove only for testing
                R.id.menu_notification_test -> {
                    Log.i(TAG, "calling notification")
                    Log.i(
                        TAG,
                        NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()
                            .toString()
                    )
                    NotificationHelper.createNotificationChannel()
                    NotificationHelper.sendNotification(
                        getString(R.string.notification_headline),
                        getString(R.string.notification_body),
                        NotificationCompat.PRIORITY_HIGH
                    )
                    true
                }
                else -> super.onOptionsItemSelected(it)
            }
        }
        popup.show()
    }
}
