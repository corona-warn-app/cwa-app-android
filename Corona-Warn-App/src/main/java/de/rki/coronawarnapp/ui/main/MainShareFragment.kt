package de.rki.coronawarnapp.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentMainShareBinding
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel
import de.rki.coronawarnapp.util.ExternalActionHelper

/**
 * This fragment informs the user about what he is going to share and how he is going to help everybody with this :)
 *
 * @see TracingViewModel
 */
class MainShareFragment : Fragment() {

    companion object {
        private val TAG: String? = MainShareFragment::class.simpleName
    }

    private val tracingViewModel: TracingViewModel by activityViewModels()
    private var _binding: FragmentMainShareBinding? = null
    private val binding: FragmentMainShareBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainShareBinding.inflate(inflater)
        binding.tracingViewModel = tracingViewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
    }

    private fun setButtonOnClickListener() {
        binding.mainShareButton.setOnClickListener {
            ExternalActionHelper.shareText(this, getString(R.string.main_share_message), null)
        }
        binding.mainShareHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mainShareContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
