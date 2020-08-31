package de.rki.coronawarnapp.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentOnboardingTargetDeBinding
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.util.DialogHelper
import kotlinx.android.synthetic.main.include_target_de.view.*

/**
 * Onboarding starting point.
 */
class OnboardingTargetDe : Fragment() {
    companion object {
        private val TAG: String? = OnboardingTargetDe::class.simpleName
    }

    private var _binding: FragmentOnboardingTargetDeBinding? = null
    private val binding: FragmentOnboardingTargetDeBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOnboardingTargetDeBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.onboardingButtonNext.setOnClickListener {
            findNavController().doNavigate(
                OnboardingFragmentDirections.actionOnboardingFragmentToOnboardingPrivacyFragment()
            )
        }
        setButtonOnClickListener()
    }


    private fun setButtonOnClickListener() {
        binding.targetDe.targetDeNotSure.setOnClickListener {
            showCancelDialog()
        }
        binding.targetDe.tracingStatusCardBtn1.setOnClickListener {
            val constraintLayout = binding.targetDe.tracingStatusCardBtn1
            changeState(constraintLayout)
        }
        binding.targetDe.tracingStatusCardBtn2.setOnClickListener {
            val constraintLayout = binding.targetDe.tracingStatusCardBtn2
            changeState(constraintLayout)
        }
    }

    private fun changeState(constraintLayout: ConstraintLayout) {
        val context = constraintLayout.context
        var color = context.getColorStateList(R.color.colorInterBtnNotPressed)
        var textColor = context.getColor(R.color.colorTextInterBtnNotPressed)
        if (constraintLayout.backgroundTintList === color) {
            color = context.getColorStateList(R.color.colorInterBtnPressed)
            textColor = context.getColor(R.color.colorTextInterBtnPressed)
        }

        constraintLayout.backgroundTintList = color
        constraintLayout.tracing_status_card_body.setTextColor(textColor)


    }

    private fun showCancelDialog() {
        val dialog = DialogHelper.DialogInstance(
            requireActivity(),
            R.string.onboarding_tracing_dialog_headline,
            R.string.onboarding_tracing_dialog_body,
            R.string.onboarding_tracing_dialog_button_positive,
            R.string.onboarding_tracing_dialog_button_negative,
            true,
            {

            })
        DialogHelper.showDialog(dialog)
    }

    override fun onResume() {
        super.onResume()
        binding.onboardingContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
