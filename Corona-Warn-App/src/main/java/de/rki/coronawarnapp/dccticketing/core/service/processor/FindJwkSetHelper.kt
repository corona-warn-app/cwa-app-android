package de.rki.coronawarnapp.dccticketing.core.service.processor

import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingErrorCode
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingServiceIdentityDocument
import timber.log.Timber

fun DccTicketingServiceIdentityDocument.findJwkSet(jwkSetType: JwkSetType): Set<DccJWK> {
    Timber.d("findJwkSet(jwkSetType=%s)", jwkSetType)

    val jwkSet = verificationMethod
        .filter { it.id.matches(jwkSetType.regex) }
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
        regex = """/AccessTokenSignKey-\d+${'$'}/""".toRegex(),
        noMatchingEntryErrorCode = DccTicketingErrorCode.VD_ID_NO_ATS_SIGN_KEY
    ),

    AccessTokenServiceJwkSet(
        regex = """/AccessTokenServiceKey-\d+${'$'}/""".toRegex(),
        noMatchingEntryErrorCode = DccTicketingErrorCode.VD_ID_NO_ATS_SVC_KEY
    ),

    ValidationServiceJwkSet(
        regex = """/ValidationServiceKey-\d+${'$'}/""".toRegex(),
        noMatchingEntryErrorCode = DccTicketingErrorCode.VD_ID_NO_VS_SVC_KEY
    ),

    ValidationServiceSignKeyJwkSet(
        regex = """/ValidationServiceSignKey-\d+${'$'}/""".toRegex(),
        noMatchingEntryErrorCode = DccTicketingErrorCode.VS_ID_NO_SIGN_KEY
    )
}
