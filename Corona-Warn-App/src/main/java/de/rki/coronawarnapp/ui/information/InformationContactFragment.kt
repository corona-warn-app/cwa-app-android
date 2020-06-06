package de.rki.coronawarnapp.ui.information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentInformationContactBinding
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.CallHelper

/**
 * Basic Fragment which only displays static content.
 */
class InformationContactFragment : BaseFragment() {
    companion object {
        private val TAG: String? = InformationContactFragment::class.simpleName
    }

    private var _binding: FragmentInformationContactBinding? = null
    private val binding: FragmentInformationContactBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInformationContactBinding.inflate(inflater)
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
        binding.informationContactHeader.toolbar.setNavigationOnClickListener {
            (activity as MainActivity).goBack()
        }
        binding.informationContactNavigationRowPhone.navigationRow.setOnClickListener {
            CallHelper.call(
                this,
                requireContext().getString(
                    R.string.information_contact_phone_call_number
                )
            )
        }
    }
}
