package de.rki.coronawarnapp.contactdiary.ui.durationpicker

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.di.AutoInject

class ContactDiaryDurationPickerFragment : DialogFragment() {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater;

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(inflater.inflate(R.layout.contact_diary_duration_picker_dialog_fragment, null))
                // Add action buttons
//                .setPositiveButton(R.string.signin,
//                    DialogInterface.OnClickListener { dialog, id ->
//                        // sign in the user ...
//                    })
//                .setNegativeButton(R.string.cancel,
//                    DialogInterface.OnClickListener { dialog, id ->
//                        getDialog().cancel()
//                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}
