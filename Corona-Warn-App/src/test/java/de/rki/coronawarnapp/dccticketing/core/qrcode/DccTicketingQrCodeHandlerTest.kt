package de.rki.coronawarnapp.dccticketing.core.qrcode

import de.rki.coronawarnapp.dccticketing.core.allowlist.DccTicketingAllowListException
import de.rki.coronawarnapp.dccticketing.core.allowlist.filtering.DccJwkFilteringResult
import de.rki.coronawarnapp.dccticketing.core.allowlist.filtering.DccTicketingJwkFilter
import de.rki.coronawarnapp.dccticketing.core.service.DccTicketingRequestService
import de.rki.coronawarnapp.dccticketing.core.service.processor.ValidationDecoratorRequestProcessor
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class DccTicketingQrCodeHandlerTest : BaseTest() {

    @MockK lateinit var requestService: DccTicketingRequestService
    @MockK lateinit var dccTicketingJwkFilter: DccTicketingJwkFilter

    private val qrcode = DccTicketingQrCode(
        qrCode = "QrCodeString",
        data = DccTicketingQrCodeData(
            privacyUrl = "privacyUrl",
            protocol = "protocol",
            protocolVersion = "protocolVersion",
            serviceProvider = "serviceProvider",
            token = "token",
            consent = "consent",
            subject = "subject",
            serviceIdentity = "serviceIdentity"
        )
    )

    private val decorator = ValidationDecoratorRequestProcessor.ValidationDecoratorResult(
        accessTokenService = mockk(),
        accessTokenServiceJwkSet = emptySet(),
        accessTokenSignJwkSet = emptySet(),
        validationService = mockk(),
        validationServiceJwkSet = emptySet()
    )

    private val mockkJwt = mockk<DccJWK>()
    private val filteringResult = DccJwkFilteringResult(
        filteredJwkSet = setOf(mockkJwt),
        filteredAllowlist = emptySet()
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `handleQrCode decorate TransactionContext`() = runBlockingTest {
        coEvery { dccTicketingJwkFilter.filter(any()) } returns filteringResult
        coEvery { requestService.requestValidationDecorator(any()) } returns decorator

        instance().handleQrCode(qrcode) shouldBe
            DccTicketingTransactionContext(
                initializationData = qrcode.data,
                accessTokenService = decorator.accessTokenService,
                accessTokenServiceJwkSet = decorator.accessTokenServiceJwkSet,
                accessTokenSignJwkSet = decorator.accessTokenSignJwkSet,
                validationService = decorator.validationService,
                validationServiceJwkSet = decorator.validationServiceJwkSet,
            )

        coVerifySequence {
            requestService.requestValidationDecorator(any())
            dccTicketingJwkFilter.filter(any())
        }
    }

    @Test
    fun `handleQrCode throws ALLOWLIST_NO_MATCH error`() = runBlockingTest {
        coEvery { dccTicketingJwkFilter.filter(any()) } returns DccJwkFilteringResult(emptySet(), emptySet())
        coEvery { requestService.requestValidationDecorator(any()) } returns decorator

        shouldThrow<DccTicketingAllowListException> {
            instance().handleQrCode(qrcode) shouldBe
                DccTicketingTransactionContext(
                    initializationData = qrcode.data,
                    accessTokenService = decorator.accessTokenService,
                    accessTokenServiceJwkSet = decorator.accessTokenServiceJwkSet,
                    accessTokenSignJwkSet = decorator.accessTokenSignJwkSet,
                    validationService = decorator.validationService,
                    validationServiceJwkSet = decorator.validationServiceJwkSet,
                )
        }.errorCode shouldBe DccTicketingAllowListException.ErrorCode.ALLOWLIST_NO_MATCH
    }

    private fun instance() = DccTicketingQrCodeHandler(
        requestService = requestService,
        dccTicketingJwkFilter = dccTicketingJwkFilter
    )
}
