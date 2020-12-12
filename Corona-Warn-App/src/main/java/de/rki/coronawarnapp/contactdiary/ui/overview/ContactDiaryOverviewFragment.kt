package de.rki.coronawarnapp.contactdiary.ui.overview

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.rki.coronawarnapp.R

class ContactDiaryOverviewFragment : Fragment() {

    companion object {
        fun newInstance() = ContactDiaryOverviewFragment()
    }

    private lateinit var viewModel: ContactDiaryOverviewViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.contact_diary_overview_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ContactDiaryOverviewViewModel::class.java)
        // TODO: Use the ViewModel
    }
}
