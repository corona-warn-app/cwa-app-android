package de.rki.coronawarnapp.test.dccticketing

import android.os.Bundle
import android.view.View
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentTestDccTicketingBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat
import de.rki.coronawarnapp.util.encoding.base64
import de.rki.coronawarnapp.util.ui.viewBinding

@AndroidEntryPoint
class DccTicketingTestFragment : Fragment(R.layout.fragment_test_dcc_ticketing) {

    private val viewModel: DccTicketingTestViewModel by viewModels()
    private val binding: FragmentTestDccTicketingBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            generateEcKeyPair.setOnClickListener {
                viewModel.generateECKeyPair()
            }
            consentOneButton.setOnClickListener {
                findNavController().navigate(R.id.dcc_ticketing_nav_graph)
            }

            viewModel.checkServiceIdentity.observe(viewLifecycleOwner) { swichServiceIdentityCheck.isChecked = it }

            swichServiceIdentityCheck.setOnCheckedChangeListener { _, isChecked ->
                viewModel.toggleServiceIdentityCheck(isChecked)
            }
        }
        val color = requireContext().getColorCompat(R.color.colorPrimary)
        viewModel.ecKeyPair.observe(viewLifecycleOwner) {
            binding.generatedKeyPair.text = buildSpannedString {
                color(color) {
                    append("PublicKeyBase64=")
                }
                append(it.publicKeyBase64)
                appendLine()
                color(color) {
                    append("PrivateKeyBase64=")
                }
                append(it.privateKey.encoded.base64())
            }
        }
    }

    companion object {
        val MENU_ITEM = TestMenuItem(
            title = "DCC Ticketing",
            description = "Dcc Ticketing validation",
            targetId = R.id.dccTicketingTestFragment
        )
    }
}
