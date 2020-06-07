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

    private var _binding: FragmentMainOverviewBinding? = null
    private val binding: FragmentMainOverviewBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainOverviewBinding.inflate(inflater)
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
        binding.mainOverviewHeader.headerToolbar.setNavigationOnClickListener {
            (activity as MainActivity).goBack()
        }
    }
}
