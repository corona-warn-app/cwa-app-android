package de.rki.coronawarnapp.ui.submission

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionDispatcherBinding
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.util.CameraPermissionHelper

class SubmissionDispatcherFragment : BaseFragment() {

    companion object {
        private const val REQUEST_CAMERA_PERMISSION_CODE = 1
        private val TAG: String? = SubmissionDispatcherFragment::class.simpleName
    }

    private lateinit var binding: FragmentSubmissionDispatcherBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSubmissionDispatcherBinding.inflate(inflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
    }

    private fun setButtonOnClickListener() {
        binding.submissionDispatcherQr.dispatcherCard.setOnClickListener {
            checkForCameraPermission()
        }
        binding.submissionDispatcherTanCode.dispatcherCard.setOnClickListener {
            doNavigate(
                SubmissionDispatcherFragmentDirections
                    .actionSubmissionDispatcherFragmentToSubmissionTanFragment()
            )
        }
        binding.submissionDispatcherTanTele.dispatcherCard.setOnClickListener {
            Log.i(TAG, "TAN tele pressed")
        }
    }

    private fun checkForCameraPermission() {
        if (!CameraPermissionHelper.hasCameraPermission(requireActivity())) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                showCameraPermissionRationaleDialog()
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_CAMERA_PERMISSION_CODE
                )
            }
        } else {
            cameraPermissionIsGranted()
        }
    }

    private fun showCameraPermissionRationaleDialog() {
        val alertDialog: AlertDialog = requireActivity().let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle(R.string.submission_qr_code_scan_permission_rationale_dialog_headline)
                setMessage(R.string.submission_qr_code_scan_permission_rationale_dialog_body)
                setPositiveButton(
                    R.string.submission_qr_code_scan_permission_rationale_dialog_button_positive
                ) { _, _ ->
                    requestPermissions(
                        arrayOf(Manifest.permission.CAMERA),
                        REQUEST_CAMERA_PERMISSION_CODE
                    )
                }
                setNegativeButton(
                    R.string.submission_qr_code_scan_permission_rationale_dialog_button_negative
                ) { _, _ -> }
            }
            builder.create()
        }
        alertDialog.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            cameraPermissionIsGranted()
        } else {
            showCameraPermissionDeniedDialog()
        }
    }

    private fun showCameraPermissionDeniedDialog() {
        val alertDialog: AlertDialog = requireActivity().let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle(R.string.submission_qr_code_scan_permission_denied_dialog_headline)
                setMessage(R.string.submission_qr_code_scan_permission_denied_dialog_body)
                setPositiveButton(
                    R.string.submission_qr_code_scan_permission_denied_dialog_button_positive
                ) { _, _ -> }
            }
            builder.create()
        }
        alertDialog.show()
    }

    private fun cameraPermissionIsGranted() {
        doNavigate(
            SubmissionDispatcherFragmentDirections
                .actionSubmissionDispatcherFragmentToSubmissionQRCodeScanFragment()
        )
    }
}
