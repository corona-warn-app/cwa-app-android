package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.server.protocols.KeyExportFormat

object SignatureHelper {
    val clientSig: KeyExportFormat.SignatureInfo = KeyExportFormat.SignatureInfo.newBuilder()
        .setAndroidPackage("de.rki.coronawarnapp")
        .setAppBundleId("de.rki.coronawarnapp")
        .setSignatureAlgorithm("ECDSA")
        .build()
}
