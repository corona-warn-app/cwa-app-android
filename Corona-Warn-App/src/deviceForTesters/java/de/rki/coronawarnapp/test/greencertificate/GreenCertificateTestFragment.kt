package de.rki.coronawarnapp.test.greencertificate

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestGreenCertificateBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

@SuppressLint("SetTextI18n")
class GreenCertificateTestFragment : Fragment(R.layout.fragment_test_green_certificate), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: GreenCertificateTestFragmentViewModel by cwaViewModels { viewModelFactory }
    private val binding by viewBinding<FragmentTestGreenCertificateBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        val MENU_ITEM = TestMenuItem(
            title = "Green Certificate",
            description = "View & Control green certificate related features.",
            targetId = R.id.greenCertificateTestFragment
        )
    }
}
