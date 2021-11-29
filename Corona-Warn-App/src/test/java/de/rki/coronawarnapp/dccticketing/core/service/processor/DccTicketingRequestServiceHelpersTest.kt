package de.rki.coronawarnapp.dccticketing.core.service.processor

import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingErrorCode
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingServiceIdentityDocument
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingVerificationMethod
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DccTicketingRequestServiceHelpersTest : BaseTest() {

    private val emptyServiceIdentityDocument = DccTicketingServiceIdentityDocument(
        id = "DccTicketingServiceIdentityDocument Test ID",
        verificationMethod = emptyList(),
        _service = emptyList()
    )

    private val emptyVerificationMethod = DccTicketingVerificationMethod(
        id = "DccTicketingVerificationMethod Test id",
        type = "DccTicketingVerificationMethod Test type",
        controller = "DccTicketingVerificationMethod Test controller",
        publicKeyJwk = null,
        verificationMethods = null
    )

    private val emptyJwk = DccJWK(
        x5c = emptyList(),
        kid = "DccJWK Test kid",
        alg = "DccJWK Test alg",
        use = DccJWK.Purpose.SIGNATURE
    )

    private val accessTokenSignKeyID =
        "https://test.com/test-system/identity/verificationMethod/JsonWebKey2020#AccessTokenSignKey-1"
    private val accessTokenServiceKeyID =
        "https://test.com/test-system/identity/verificationMethod/JsonWebKey2020#AccessTokenServiceKey-1"
    private val validationServiceKeyID =
        "https://test.com/test-system/identity/verificationMethod/JsonWebKey2020#ValidationServiceKey-1"
    private val validationServiceSignKeyID =
        "https://test.com/test-system/identity/verificationMethod/JsonWebKey2020#ValidationServiceSignKey-1"

    @Test
    fun `verifyJwks() - document passes`() {
        val errorCode = DccTicketingErrorCode.VD_ID_EMPTY_X5C
        val serviceIdentityDocumentWithEmptyVerificationMethod = emptyServiceIdentityDocument.copy(
            verificationMethod = listOf(emptyVerificationMethod)
        )

        val serviceIdentityDocumentWithProperJWK = emptyServiceIdentityDocument.copy(
            verificationMethod = listOf(
                emptyVerificationMethod.copy(
                    publicKeyJwk = emptyJwk.copy(
                        x5c = listOf("Not empty x5c")
                    )
                )
            )
        )

        shouldNotThrowAny {
            emptyServiceIdentityDocument.verifyJwks(emptyX5cErrorCode = errorCode)
            serviceIdentityDocumentWithEmptyVerificationMethod.verifyJwks(emptyX5cErrorCode = errorCode)
            serviceIdentityDocumentWithProperJWK.verifyJwks(emptyX5cErrorCode = errorCode)
        }
    }

    @Test
    fun `verifyJwks() - document fails`() {
        val errorCode = DccTicketingErrorCode.VS_ID_EMPTY_X5C
        val serviceIdentityDocumentWithBadJWK = emptyServiceIdentityDocument.copy(
            verificationMethod = listOf(
                emptyVerificationMethod.copy(publicKeyJwk = emptyJwk)
            )
        )

        shouldThrow<DccTicketingException> {
            serviceIdentityDocumentWithBadJWK.verifyJwks(emptyX5cErrorCode = errorCode)
        }.errorCode shouldBe errorCode
    }

    @Test
    fun `findJwkSet() - document contains required verification method with jwk`() {
        val accessTokenSignKeyMethod = createVerificationMethod(accessTokenSignKeyID)
        val accessTokenServiceKeyMethod = createVerificationMethod(accessTokenServiceKeyID)
        val validationServiceKeyMethod = createVerificationMethod(validationServiceKeyID)
        val validationServiceSignKeyMethod = createVerificationMethod(validationServiceSignKeyID)
        val document = emptyServiceIdentityDocument.copy(
            verificationMethod = listOf(
                accessTokenServiceKeyMethod,
                accessTokenSignKeyMethod,
                validationServiceKeyMethod,
                validationServiceSignKeyMethod
            )
        )

        document.findJwkSet(jwkSetType = JwkSetType.AccessTokenSignJwkSet).run {
            size shouldBe 1
            first().kid shouldBe accessTokenSignKeyID
        }

        document.findJwkSet(jwkSetType = JwkSetType.AccessTokenServiceJwkSet).run {
            size shouldBe 1
            first().kid shouldBe accessTokenServiceKeyID
        }

        document.findJwkSet(jwkSetType = JwkSetType.ValidationServiceJwkSet).run {
            size shouldBe 1
            first().kid shouldBe validationServiceKeyID
        }

        document.findJwkSet(jwkSetType = JwkSetType.ValidationServiceSignKeyJwkSet).run {
            size shouldBe 1
            first().kid shouldBe validationServiceSignKeyID
        }
    }

    @Test
    fun `findJwkSet() - document doesn't contain required verification method`() {
        val accessTokenSignKeyMethod = createVerificationMethod("$accessTokenSignKeyID/doNotFindMe")
        val accessTokenServiceKeyMethod = createVerificationMethod(accessTokenServiceKeyID)
        val validationServiceKeyMethod = createVerificationMethod(validationServiceKeyID)
        val validationServiceSignKeyMethod = createVerificationMethod(validationServiceSignKeyID)
        val document = emptyServiceIdentityDocument.copy(
            verificationMethod = listOf(
                accessTokenSignKeyMethod,
                accessTokenServiceKeyMethod,
                validationServiceKeyMethod,
                validationServiceSignKeyMethod
            )
        )

        JwkSetType.AccessTokenSignJwkSet.also {
            shouldThrow<DccTicketingException> {
                emptyServiceIdentityDocument.findJwkSet(jwkSetType = it)
            }.errorCode shouldBe it.noMatchingEntryErrorCode

            shouldThrow<DccTicketingException> {
                document.findJwkSet(jwkSetType = it)
            }.errorCode shouldBe it.noMatchingEntryErrorCode
        }
    }

    @Test
    fun `findJwkSet() - document contains required verification method but without jwk`() {
        val accessTokenSignKeyMethod = createVerificationMethod(id = accessTokenSignKeyID, jwk = null)
        val accessTokenServiceKeyMethod = createVerificationMethod(id = accessTokenServiceKeyID, jwk = null)
        val validationServiceKeyMethod = createVerificationMethod(id = validationServiceKeyID, jwk = null)
        val validationServiceSignKeyMethod = createVerificationMethod(id = validationServiceSignKeyID, jwk = null)
        val document = emptyServiceIdentityDocument.copy(
            verificationMethod = listOf(
                accessTokenServiceKeyMethod,
                accessTokenSignKeyMethod,
                validationServiceKeyMethod,
                validationServiceSignKeyMethod
            )
        )

        JwkSetType.AccessTokenSignJwkSet.also {
            shouldThrow<DccTicketingException> {
                document.findJwkSet(jwkSetType = it)
            }.errorCode shouldBe it.noMatchingEntryErrorCode
        }

        JwkSetType.AccessTokenServiceJwkSet.also {
            shouldThrow<DccTicketingException> {
                document.findJwkSet(jwkSetType = it)
            }.errorCode shouldBe it.noMatchingEntryErrorCode
        }

        JwkSetType.ValidationServiceJwkSet.also {
            shouldThrow<DccTicketingException> {
                document.findJwkSet(jwkSetType = it)
            }.errorCode shouldBe it.noMatchingEntryErrorCode
        }

        JwkSetType.ValidationServiceSignKeyJwkSet.also {
            shouldThrow<DccTicketingException> {
                document.findJwkSet(jwkSetType = it)
            }.errorCode shouldBe it.noMatchingEntryErrorCode
        }
    }

    private fun createVerificationMethod(id: String, jwk: DccJWK? = emptyJwk) = emptyVerificationMethod.copy(
        id = id,
        publicKeyJwk = jwk?.copy(kid = id)
    )
}
