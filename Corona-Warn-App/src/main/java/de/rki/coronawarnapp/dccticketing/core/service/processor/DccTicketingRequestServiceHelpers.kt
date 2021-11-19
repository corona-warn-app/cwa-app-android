package de.rki.coronawarnapp.dccticketing.core.service.processor

import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingErrorCode
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingServiceIdentityDocument
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingVerificationMethod
import timber.log.Timber

/**
 * @throws [DccTicketingException] if there is any element in [DccTicketingServiceIdentityDocument.verificationMethod]
 * where [DccTicketingVerificationMethod.publicKeyJwk] is set and [DccJWK.x5c] does not contain at least one element,
 * the operation shall abort with the specified error code, otherwise the document is valid
 */
fun DccTicketingServiceIdentityDocument.verifyJwks(emptyX5cErrorCode: DccTicketingErrorCode) {
    Timber.d("verifyJwks(emptyX5cErrorCode=%s)", emptyX5cErrorCode)

    val hasAnyEmptyX5c = verificationMethod
        .mapNotNull { it.publicKeyJwk }
        .any { it.x5c.isEmpty() }

    when (hasAnyEmptyX5c) {
        false -> Timber.d("Verified document=%s", this)
        true -> throw DccTicketingException(errorCode = emptyX5cErrorCode)
    }
}

fun DccTicketingServiceIdentityDocument.findJwkSet(jwkSetType: JwkSetType): Set<DccJWK> {
    Timber.d("findJwkSet(jwkSetType=%s)", jwkSetType)

    val jwkSet = verificationMethod
        .filter { jwkSetType.regex.containsMatchIn(it.id) }
        .mapNotNull { it.publicKeyJwk }
        .toSet()

    if (jwkSet.isEmpty()) {
        Timber.d("No matching entries for %s, aborting", jwkSetType)
        throw DccTicketingException(errorCode = jwkSetType.noMatchingEntryErrorCode)
    }
    return jwkSet.also { Timber.d("Found %s=%s", jwkSetType.name, jwkSet) }
}

enum class JwkSetType(
    val regex: Regex,
    val noMatchingEntryErrorCode: DccTicketingErrorCode
) {
    AccessTokenSignJwkSet(
        regex = """AccessTokenSignKey-\d+${'$'}""".toRegex(),
        noMatchingEntryErrorCode = DccTicketingErrorCode.VD_ID_NO_ATS_SIGN_KEY
    ),

    AccessTokenServiceJwkSet(
        regex = """AccessTokenServiceKey-\d+${'$'}""".toRegex(),
        noMatchingEntryErrorCode = DccTicketingErrorCode.VD_ID_NO_ATS_SVC_KEY
    ),

    ValidationServiceJwkSet(
        regex = """ValidationServiceKey-\d+${'$'}""".toRegex(),
        noMatchingEntryErrorCode = DccTicketingErrorCode.VD_ID_NO_VS_SVC_KEY
    ),

    ValidationServiceSignKeyJwkSet(
        regex = """ValidationServiceSignKey-\d+${'$'}""".toRegex(),
        noMatchingEntryErrorCode = DccTicketingErrorCode.VS_ID_NO_SIGN_KEY
    )
}
