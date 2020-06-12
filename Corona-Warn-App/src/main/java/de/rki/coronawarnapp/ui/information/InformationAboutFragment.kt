package de.rki.coronawarnapp.ui.information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.databinding.FragmentInformationAboutBinding
import de.rki.coronawarnapp.ui.main.MainActivity

/**
 * Basic Fragment which only displays static content.
 */
class InformationAboutFragment : Fragment() {
    companion object {
        private val TAG: String? = InformationAboutFragment::class.simpleName
    }

    private var _binding: FragmentInformationAboutBinding? = null
    private val binding: FragmentInformationAboutBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInformationAboutBinding.inflate(inflater)
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

    override fun onResume() {
        super.onResume()
        binding.informationAboutContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        binding.informationAboutHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }
    }
}
