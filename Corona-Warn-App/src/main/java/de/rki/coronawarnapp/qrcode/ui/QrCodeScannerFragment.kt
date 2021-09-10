package de.rki.coronawarnapp.qrcode.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.di.AutoInject

class QrCodeScannerFragment : Fragment(R.layout.fragment_scan_qr_code), AutoInject {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO
    }
}
