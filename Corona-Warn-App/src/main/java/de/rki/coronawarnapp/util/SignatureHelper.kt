package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.server.protocols.external.exposurenotification.TemporaryExposureKeyExportOuterClass.SignatureInfo

object SignatureHelper {
    val clientSig: SignatureInfo = SignatureInfo.newBuilder()
        .setAndroidPackage("de.rki.coronawarnapp")
        .setAppBundleId("de.rki.coronawarnapp")
        .setSignatureAlgorithm("ECDSA")
        .build()
}
