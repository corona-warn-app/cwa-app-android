package de.rki.coronawarnapp.ui.information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.rki.coronawarnapp.databinding.FragmentInformationContactBinding
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.ui.UiConstants
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.CallHelper

/**
 * Basic Fragment which only displays static content.
 */
class InformationContactFragment : BaseFragment() {
    companion object {
        private val TAG: String? = InformationContactFragment::class.simpleName
    }

    private lateinit var binding: FragmentInformationContactBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInformationContactBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setButtonOnClickListener()
    }

    private fun setButtonOnClickListener() {
        binding.informationContactHeader.informationHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }
        binding.informationContactNavigationRowPhone.navigationRow.setOnClickListener {
            CallHelper.call(this, UiConstants.TECHNICAL_HOTLINE)
        }
    }
}
