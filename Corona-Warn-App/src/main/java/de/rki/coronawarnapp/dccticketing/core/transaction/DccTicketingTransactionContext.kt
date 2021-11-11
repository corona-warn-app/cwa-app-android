package de.rki.coronawarnapp.dccticketing.core.transaction

import android.os.Parcelable
import de.rki.coronawarnapp.dccticketing.core.qrcode.DccTicketingQrCodeData
import kotlinx.android.parcel.Parcelize
import java.security.PrivateKey
import java.security.PublicKey

@Parcelize
data class DccTicketingTransactionContext(
    val initializationData: DccTicketingQrCodeData,
    val accessTokenService: tbd = null,
    val accessTokenServiceJwkSet: tbd = null,
    val accessTokenSignJwkSet: tbd = null,
    val validationService: String? = null,
    val validationServiceJwkSet: tbd = null,
    val validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESCBC: tbd = null,
    val validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM: tbd = null,
    val validationServiceSignKeyJwkSet: tbd = null,
    val ecPublicKey: PublicKey? = null,
    val ecPrivateKey: PrivateKey? = null,
    val ecPublicKeyBase64: String? = null,
    val accessToken: String? = null,
    val accessTokenPayload: DccTicketingAccessToken? = null,
    val nonceBase64: String? = null,
    val dccBarcodeData: String? = null,
    val encryptedDCCBase64: String? = null,
    val encryptionKeyBase64: tbd = null,
    val signatureBase64: String? = null,
    val signatureAlgorithm: String? = null,
    val resultToken: DccTicketingResultToken? = null,
    val resultTokenPayload: tbd = null,
) : Parcelable

typealias tbd = String? // todo remove placeholder
