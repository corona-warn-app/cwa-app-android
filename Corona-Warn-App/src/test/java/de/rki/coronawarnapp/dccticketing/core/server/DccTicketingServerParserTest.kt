package de.rki.coronawarnapp.dccticketing.core.server

import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingServiceIdentityDocument
import de.rki.coronawarnapp.dccticketing.core.server.DccTicketingServerException.ErrorCode.PARSE_ERR
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingService
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingVerificationMethod
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import okhttp3.ResponseBody
import org.junit.jupiter.api.Test
import retrofit2.Response
import testhelpers.BaseTest

class DccTicketingServerParserTest : BaseTest() {

    private val mapper = SerializationModule().jacksonObjectMapper()

    private val instance: DccTicketingServerParser
        get() = DccTicketingServerParser(
            mapper = mapper
        )

    @Test
    fun `happy path`() {
        val emptyDocument = DccTicketingServiceIdentityDocument(
            id = "emptyDocument",
            verificationMethod = emptyList(),
            _service = null
        )

        val filledDocument = DccTicketingServiceIdentityDocument(
            id = "filledDocument",
            verificationMethod = listOf(
                DccTicketingVerificationMethod(
                    id = "id",
                    type = "type",
                    controller = "controller",
                    publicKeyJwk = DccJWK(
                        x5c = listOf("x5c"),
                        kid = "kid",
                        alg = "alg",
                        use = DccJWK.Purpose.ENCRYPTION
                    ),
                    verificationMethods = listOf("verificationMethods")
                )
            ),
            _service = listOf(
                DccTicketingService(
                    id = "id",
                    type = "type",
                    serviceEndpoint = "serviceEndpoint",
                    name = "name"
                )
            )
        )

        val emptyDocumentResponse = createResponse(mapper.writeValueAsString(emptyDocument))
        val filledDocumentResponse = createResponse(mapper.writeValueAsString(filledDocument))

        with(instance) {
            createServiceIdentityDocument(response = emptyDocumentResponse) shouldBe emptyDocument
            createServiceIdentityDocument(response = filledDocumentResponse) shouldBe filledDocument
        }
    }

    @Test
    fun `throws DccTicketingServerException with error code PARSE_ERR on any error case`() {
        with(instance) {
            val emptyResponse = createResponse(null)

            shouldThrow<DccTicketingServerException> {
                createServiceIdentityDocument(response = emptyResponse)
            }.errorCode shouldBe PARSE_ERR

            val faultyResponse = createResponse("DccTicketingServiceIdentityDocument")

            shouldThrow<DccTicketingServerException> {
                createServiceIdentityDocument(response = faultyResponse)
            }.errorCode shouldBe PARSE_ERR
        }
    }

    private fun createResponse(response: String?): Response<ResponseBody> = mockk {
        every { body() } returns when (response == null) {
            true -> null
            false -> mockk {
                every { charStream() } returns response.reader()
            }
        }
    }
}
