package de.rki.coronawarnapp.dccticketing.core.transaction

import android.os.Parcelable
import de.rki.coronawarnapp.dccticketing.core.qrcode.DccTicketingQrCodeData
import kotlinx.android.parcel.Parcelize
import java.security.PrivateKey
import java.security.PublicKey

@Parcelize
data class DccTicketingTransactionContext(
    val initializationData: DccTicketingQrCodeData,
    val accessTokenService: DccTicketingService? = null,
    val accessTokenServiceJwkSet: Set<DccJWK>? = null,
    val accessTokenSignJwkSet: Set<DccJWK>? = null,
    val validationService: String? = null,
    val validationServiceJwkSet: Set<DccJWK>? = null,
    val validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESCBC: Set<DccJWK>? = null,
    val validationServiceEncKeyJwkSetForRSAOAEPWithSHA256AESGCM: Set<DccJWK>? = null,
    val validationServiceSignKeyJwkSet: Set<DccJWK>? = null,
    val ecPublicKey: PublicKey? = null,
    val ecPrivateKey: PrivateKey? = null,
    val ecPublicKeyBase64: String? = null,
    val accessToken: String? = null,
    val accessTokenPayload: DccTicketingAccessToken? = null,
    val nonceBase64: String? = null,
    val dccBarcodeData: String? = null,
    val encryptedDCCBase64: String? = null,
    val encryptionKeyBase64: String? = null,
    val signatureBase64: String? = null,
    val signatureAlgorithm: String? = null,
    val resultToken: String? = null,
    val resultTokenPayload: DccTicketingResultToken? = null,
) : Parcelable

typealias tbd = String? // todo remove placeholder
