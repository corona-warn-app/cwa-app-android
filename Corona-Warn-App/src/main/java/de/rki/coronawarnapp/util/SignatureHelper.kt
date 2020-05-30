package de.rki.coronawarnapp.util

import KeyExportFormat.SignatureInfo

object SignatureHelper {
    val clientSig: SignatureInfo = SignatureInfo.newBuilder()
        .setAndroidPackage("de.rki.coronawarnapp")
        .setAppBundleId("de.rki.coronawarnapp")
        .setSignatureAlgorithm("ECDSA")
        .build()
}
