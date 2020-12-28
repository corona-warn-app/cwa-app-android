package de.rki.coronawarnapp.ui.main.home.popups

import androidx.appcompat.app.AlertDialog
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.main.home.HomeFragment
import javax.inject.Inject

class DeviceTimeIncorrectDialog @Inject constructor(
    private val homeFragment: HomeFragment
) {

    fun show(onAcknowledged: () -> Unit) {
        AlertDialog.Builder(homeFragment.requireContext()).apply {
            setTitle(R.string.device_time_incorrect_dialog_headline)
            setMessage(R.string.device_time_incorrect_dialog_body)
            setPositiveButton(R.string.device_time_incorrect_dialog_button_confirm) { _, _ ->
                onAcknowledged()
            }
        }.show()
    }
}
