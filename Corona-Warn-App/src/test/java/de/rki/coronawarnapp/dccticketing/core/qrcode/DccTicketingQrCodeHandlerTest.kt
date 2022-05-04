package de.rki.coronawarnapp.dccticketing.core.qrcode

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingAllowListContainer
import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingServiceProviderAllowListEntry
import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingValidationServiceAllowListEntry
import de.rki.coronawarnapp.dccticketing.core.allowlist.filtering.DccJwkFilteringResult
import de.rki.coronawarnapp.dccticketing.core.allowlist.filtering.DccTicketingJwkFilter
import de.rki.coronawarnapp.dccticketing.core.allowlist.internal.DccTicketingAllowListException
import de.rki.coronawarnapp.dccticketing.core.allowlist.repo.DccTicketingAllowListRepository
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.service.DccTicketingRequestService
import de.rki.coronawarnapp.dccticketing.core.service.processor.ValidationDecoratorRequestProcessor
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingTransactionContext
import de.rki.coronawarnapp.environment.BuildConfigWrap
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class DccTicketingQrCodeHandlerTest : BaseTest() {

    @MockK lateinit var requestService: DccTicketingRequestService
    @MockK lateinit var dccTicketingJwkFilter: DccTicketingJwkFilter
    @MockK lateinit var allowListRepository: DccTicketingAllowListRepository
    @MockK lateinit var qrCodeSettings: DccTicketingQrCodeSettings
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var configData: ConfigData

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
    private val validationServiceAllowList = setOf(
        DccTicketingValidationServiceAllowListEntry(
            serviceProvider = "serviceProvider",
            hostname = "eu.service.com",
            fingerprint256 = "fingerprint256".decodeBase64()!!.sha256()
        )
    )

    private val serviceProviderAllowListEntry = DccTicketingServiceProviderAllowListEntry(
        serviceIdentityHash = "serviceIdentity".toHash()
    )
    private val serviceProviderAllowList = setOf(serviceProviderAllowListEntry)

    private val filteringResult = DccJwkFilteringResult(
        filteredJwkSet = setOf(mockkJwt),
        filteredAllowlist = validationServiceAllowList
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        mockkObject(BuildConfigWrap)

        coEvery { allowListRepository.refresh() } returns DccTicketingAllowListContainer(
            validationServiceAllowList = validationServiceAllowList,
            serviceProviderAllowList = serviceProviderAllowList
        )
        every { qrCodeSettings.checkServiceIdentity.value } returns true
        every { BuildConfigWrap.VERSION_CODE } returns 2150002
        every { appConfigProvider.currentConfig } returns flowOf(configData)
        every { configData.validationServiceMinVersion } returns 0
    }

    @Test
    fun `handleQrCode decorate TransactionContext`() = runTest {
        coEvery { dccTicketingJwkFilter.filter(any(), any()) } returns filteringResult
        coEvery { requestService.requestValidationDecorator(any(), any()) } returns decorator

        instance().handleQrCode(qrcode) shouldBe
            DccTicketingTransactionContext(
                initializationData = qrcode.data,
                accessTokenService = decorator.accessTokenService,
                accessTokenServiceJwkSet = decorator.accessTokenServiceJwkSet,
                accessTokenSignJwkSet = decorator.accessTokenSignJwkSet,
                validationService = decorator.validationService,
                validationServiceJwkSet = setOf(mockkJwt),
                allowlist = validationServiceAllowList,
            )

        coVerifySequence {
            requestService.requestValidationDecorator(any(), any())
            dccTicketingJwkFilter.filter(any(), any())
        }
    }

    @Test
    fun `handleQrCode throws MIN_VERSION_REQUIRED`() = runTest {
        every { configData.validationServiceMinVersion } returns 2160101
        coEvery { dccTicketingJwkFilter.filter(any(), any()) } returns filteringResult
        coEvery { requestService.requestValidationDecorator(any(), any()) } returns decorator

        shouldThrow<DccTicketingException> {
            instance().handleQrCode(qrcode)
        }.errorCode shouldBe DccTicketingException.ErrorCode.MIN_VERSION_REQUIRED

        coVerify(exactly = 0) {
            requestService.requestValidationDecorator(any(), any())
            dccTicketingJwkFilter.filter(any(), any())
        }
    }

    @Test
    fun `handleQrCode  pass when minVersion is older`() = runTest {
        every { configData.validationServiceMinVersion } returns 2140000
        coEvery { dccTicketingJwkFilter.filter(any(), any()) } returns filteringResult
        coEvery { requestService.requestValidationDecorator(any(), any()) } returns decorator

        shouldNotThrow<DccTicketingException> {
            instance().handleQrCode(qrcode)
        }

        coVerify {
            requestService.requestValidationDecorator(any(), any())
            dccTicketingJwkFilter.filter(any(), any())
        }
    }

    @Test
    fun `handleQrCode pass when versions are the same`() = runTest {
        every { configData.validationServiceMinVersion } returns 2150002
        coEvery { dccTicketingJwkFilter.filter(any(), any()) } returns filteringResult
        coEvery { requestService.requestValidationDecorator(any(), any()) } returns decorator

        shouldNotThrow<DccTicketingException> {
            instance().handleQrCode(qrcode)
        }

        coVerify {
            requestService.requestValidationDecorator(any(), any())
            dccTicketingJwkFilter.filter(any(), any())
        }
    }

    @Test
    fun `no service provider check`() = runTest {
        coEvery { allowListRepository.refresh() } returns DccTicketingAllowListContainer(
            validationServiceAllowList = validationServiceAllowList,
            serviceProviderAllowList = emptySet()
        )
        coEvery { dccTicketingJwkFilter.filter(any(), any()) } returns filteringResult
        coEvery { requestService.requestValidationDecorator(any(), any()) } returns decorator
        every { qrCodeSettings.checkServiceIdentity.value } returns false

        shouldNotThrow<Exception> {
            instance().handleQrCode(qrcode)
        }
    }

    @Test
    fun `handleQrCode throws ALLOWLIST_NO_MATCH error`() = runTest {
        coEvery { dccTicketingJwkFilter.filter(any(), any()) } returns DccJwkFilteringResult(emptySet(), emptySet())
        coEvery { requestService.requestValidationDecorator(any(), any()) } returns decorator

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
        dccTicketingJwkFilter = dccTicketingJwkFilter,
        allowListRepository = allowListRepository,
        qrCodeSettings = qrCodeSettings,
        appConfigProvider = appConfigProvider
    )
}
