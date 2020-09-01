package de.rki.coronawarnapp.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.widget.TextView
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
        binding.onboardingTargetDeButtonNext.setOnClickListener {
            findNavController().doNavigate(
                OnboardingFragmentDirections.actionOnboardingFragmentToOnboardingPrivacyFragment()
            )
        }
        setButtonOnClickListener()
    }


    private fun setButtonOnClickListener() {
        binding.targetDe.targetDeVerify.setOnClickListener {
            showVerifyDialog()
        }

        binding.informationAboutHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as OnboardingActivity).goBack()
        }

        binding.targetDe.targetDeBtnApply.setOnClickListener {
            val constraintLayout = binding.targetDe.targetDeBtnApply
            val textView = binding.targetDe.targetDeBtnApply.target_de_apply
            val initStateLayout: Array<ConstraintLayout> =
                arrayOf(binding.targetDe.targetDeBtnReject, binding.targetDe.targetDeBtnVerify)
            val initStateTextView: Array<TextView> = arrayOf(
                binding.targetDe.targetDeBtnReject.target_de_reject,
                binding.targetDe.targetDeBtnVerify.target_de_verify
            )

            changeState(constraintLayout, textView, initStateLayout, initStateTextView)
        }

        binding.targetDe.targetDeBtnReject.setOnClickListener {
            val constraintLayout = binding.targetDe.targetDeBtnReject
            val textView = binding.targetDe.targetDeBtnReject.target_de_reject
            val initStateLayout: Array<ConstraintLayout> =
                arrayOf(binding.targetDe.targetDeBtnApply, binding.targetDe.targetDeBtnVerify)
            val initStateTextView: Array<TextView> = arrayOf(
                binding.targetDe.targetDeBtnApply.target_de_apply,
                binding.targetDe.targetDeBtnVerify.target_de_verify
            )

            changeState(constraintLayout, textView, initStateLayout, initStateTextView)
        }
    }

    private fun changeState(
        constraintLayout: ConstraintLayout,
        textView: TextView,
        initStateLayout: Array<ConstraintLayout>,
        initStateText: Array<TextView>
    ) {
        val context = constraintLayout.context
        var color = context.getColorStateList(R.color.colorInterBtnNotPressed)
        var textColor = context.getColor(R.color.colorTextInterBtnNotPressed)
        var bNextBtnValue = false

        if (constraintLayout.backgroundTintList === color) {
            color = context.getColorStateList(R.color.colorInterBtnPressed)
            textColor = context.getColor(R.color.colorTextInterBtnPressed)
            initStateLayout.forEach { entry ->
                entry.backgroundTintList =
                    context.getColorStateList(R.color.colorInterBtnNotPressed)
            }
            initStateText.forEach { entry ->
                entry.setTextColor(context.getColor(R.color.colorTextInterBtnNotPressed))
            }
            bNextBtnValue = true
        }

        enableNextButton(bNextBtnValue)
        constraintLayout.backgroundTintList = color
        textView.setTextColor(textColor)
    }

    private fun applyDialog() {
        val constraintLayout = binding.targetDe.targetDeBtnVerify
        val textView = binding.targetDe.targetDeBtnVerify.target_de_verify
        val initStateLayout: Array<ConstraintLayout> =
            arrayOf(binding.targetDe.targetDeBtnApply, binding.targetDe.targetDeBtnReject)
        val initStateTextView: Array<TextView> = arrayOf(
            binding.targetDe.targetDeBtnApply.target_de_apply,
            binding.targetDe.targetDeBtnReject.target_de_reject
        )

        changeState(constraintLayout, textView, initStateLayout, initStateTextView)
    }

    private fun enableNextButton(bValue: Boolean) {
        val nextButton = binding.onboardingTargetDeButtonNext
        nextButton.isEnabled = bValue
    }

    private fun showVerifyDialog() {
        val dialog = DialogHelper.DialogInstance(
            requireActivity(),
            R.string.target_de_verify_dialog_headline,
            R.string.target_de_verify_dialog_body,
            R.string.target_de_verify_dialog_apply,
            R.string.target_de_verify_dialog_cancel,
            true,
            ::applyDialog
        )
        DialogHelper.showDialog(dialog)
    }

    override fun onResume() {
        super.onResume()
        binding.onboardingContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
