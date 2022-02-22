package de.rki.coronawarnapp.covidcertificate.person.ui.admission

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R

class AdmissionBlockingDialog(private val context: Context) {

    private val dialog by lazy {
        MaterialAlertDialogBuilder(context).apply {
            setCancelable(false)
            setView(R.layout.admission_calculation_dialog)
        }.create()
    }

    fun setState(show: Boolean) {
        if (show && !dialog.isShowing) {
            dialog.show()
        } else if (!show && dialog.isShowing) {
            dialog.dismiss()
        }
    }
}
