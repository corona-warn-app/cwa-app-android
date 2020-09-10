package de.rki.coronawarnapp.ui.submission.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import de.rki.coronawarnapp.databinding.FragmentSubmissionCountrySelectionBinding
import de.rki.coronawarnapp.ui.submission.adapter.SubmissionCountrySelectionAdapter
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionCountrySelectViewModel

class SubmissionCountrySelectionFragment : Fragment() {

    companion object {
        private val TAG: String? = SubmissionCountrySelectionFragment::class.simpleName
    }

    private val viewModel: SubmissionCountrySelectViewModel by viewModels()
    private var _binding: FragmentSubmissionCountrySelectionBinding? = null
    private val binding: FragmentSubmissionCountrySelectionBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSubmissionCountrySelectionBinding.inflate(inflater)
        binding.submissionCountrySelectViewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = SubmissionCountrySelectionAdapter {
            viewModel.updateCountryCheckedState(it)
        }

        viewModel.countries.observe(viewLifecycleOwner, Observer {
            adapter.setCountries(it)
        })

        binding.submissionCountrySelectionSelector.submissionCountrySelectorRecyclerview.adapter =
            adapter

        binding.submissionCountrySelectionSelector.submissionCountrySelectorRecyclerview.layoutManager =
            LinearLayoutManager(context)

        binding.submissionCountrySelectionNoSelection.submissionCountryNoSelectionContainer.setOnClickListener {
            viewModel.noInfoClick()
        }

        viewModel.fetchCountries()
    }
}
