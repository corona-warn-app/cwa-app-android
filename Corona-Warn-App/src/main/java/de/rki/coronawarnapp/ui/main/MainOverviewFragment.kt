package de.rki.coronawarnapp.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.rki.coronawarnapp.databinding.FragmentMainOverviewBinding
import de.rki.coronawarnapp.ui.BaseFragment

/**
 * The fragment displays static informative content to the user
 * and represents one way to gain more detailed understanding of the
 * app and its content.
 *
 */

class MainOverviewFragment : BaseFragment() {

    companion object {
        private val TAG: String? = MainOverviewFragment::class.simpleName
    }

    private lateinit var binding: FragmentMainOverviewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainOverviewBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
    }

    private fun setButtonOnClickListener() {
        binding.mainOverviewHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }
    }
}
