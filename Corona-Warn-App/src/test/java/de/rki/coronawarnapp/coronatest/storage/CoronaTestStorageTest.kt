package de.rki.coronawarnapp.coronatest.storage

import androidx.datastore.preferences.core.stringPreferencesKey
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.storage.CoronaTestStorage.Companion.PKEY_DATA_PCR
import de.rki.coronawarnapp.coronatest.storage.CoronaTestStorage.Companion.PKEY_DATA_RA
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runTest2
import testhelpers.extensions.toComparableJsonPretty
import testhelpers.preferences.FakeDataStore
import java.io.IOException
import java.time.Instant
import java.time.LocalDate

class CoronaTestStorageTest : BaseTest() {

    lateinit var dataStore: FakeDataStore

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        dataStore = FakeDataStore()
    }

    private fun createInstance(scope: TestScope) = CoronaTestStorage(
        appScope = scope,
        dataStore = dataStore,
        baseGson = SerializationModule().baseGson()
    )

    private val pcrTest = PCRCoronaTest(
        identifier = "identifier-pcr",
        registeredAt = Instant.ofEpochMilli(1000),
        registrationToken = "regtoken-pcr",
        isSubmitted = true,
        isViewed = true,
        isAdvancedConsentGiven = true,
        isResultAvailableNotificationSent = false,
        testResult = CoronaTestResult.PCR_POSITIVE,
        testResultReceivedAt = Instant.ofEpochMilli(2000),
        lastUpdatedAt = Instant.ofEpochMilli(2001),
        isDccConsentGiven = true,
        isDccDataSetCreated = true,
    )
    private val raTest = RACoronaTest(
        identifier = "identifier-ra",
        registeredAt = Instant.ofEpochMilli(1000),
        registrationToken = "regtoken-ra",
        isSubmitted = true,
        isViewed = true,
        isAdvancedConsentGiven = true,
        isResultAvailableNotificationSent = false,
        testResult = CoronaTestResult.RAT_POSITIVE,
        testResultReceivedAt = Instant.ofEpochMilli(2000),
        firstName = "firstname",
        lastName = "lastname",
        dateOfBirth = LocalDate.parse("2021-12-24"),
        testedAt = Instant.ofEpochMilli(3000),
        lastUpdatedAt = Instant.ofEpochMilli(2001),
        isDccSupportedByPoc = true,
        isDccConsentGiven = true,
        isDccDataSetCreated = true,
    )

    private val pcrTest1 = pcrTest.copy(
        identifier = "identifier-pcr1",
        qrCodeHash = "pcrQrCodeHash"
    )

    private val raTest1 = raTest.copy(
        identifier = "identifier-ra1",
        qrCodeHash = "raQrCodeHash"
    )

    @Test
    fun `init is sideeffect free`() = runTest2 {
        createInstance(this)
    }

    @Test
    fun `storing empty set deletes data`() = runTest2 {
        dataStore[stringPreferencesKey("dontdeleteme")] = "test"
        dataStore[PKEY_DATA_RA] = "test"
        dataStore[PKEY_DATA_PCR] = "test"

        createInstance(this).updateCoronaTests(emptySet())

        dataStore[stringPreferencesKey("dontdeleteme")] shouldBe "test"
    }

    @Test
    fun `store only PCRT`() = runTest2 {
        val instance = createInstance(this)
        instance.updateCoronaTests(
            setOf(
                pcrTest.copy(
                    isProcessing = true,
                    lastError = IOException()
                )
            )
        )

        (dataStore[PKEY_DATA_PCR] as String).toComparableJsonPretty() shouldBe """
            [
                {
                    "identifier": "identifier-pcr",
                    "registeredAt": 1000,
                    "registrationToken": "regtoken-pcr",
                    "isSubmitted": true,
                    "isViewed": true,
                    "didShowBadge": false,
                    "hasResultChangeBadge":false,
                    "isAdvancedConsentGiven": true,
                    "isResultAvailableNotificationSent": false,
                    "testResultReceivedAt": 2000,
                    "testResult": 2,
                    "lastUpdatedAt": 2001,
                    "isDccSupportedByPoc": true,
                    "isDccConsentGiven": true,
                    "isDccDataSetCreated": true
                }
            ]
        """.toComparableJsonPretty()

        instance.getCoronaTests().single().apply {
            this shouldBe pcrTest.copy(
                lastError = null,
                isProcessing = false
            )
            type shouldBe BaseCoronaTest.Type.PCR
        }
    }

    @Test
    fun `store only PCRT that has QrCode Hash`() = runTest2 {
        val instance = createInstance(this)
        instance.updateCoronaTests(setOf(pcrTest1))

        (dataStore[PKEY_DATA_PCR] as String).toComparableJsonPretty() shouldBe """
            [
                {
                    "identifier": "identifier-pcr1",
                    "registeredAt": 1000,
                    "registrationToken": "regtoken-pcr",
                    "isSubmitted": true,
                    "isViewed": true,
                    "didShowBadge": false,
                    "hasResultChangeBadge":false,
                    "isAdvancedConsentGiven": true,
                    "isResultAvailableNotificationSent": false,
                    "testResultReceivedAt": 2000,
                    "testResult": 2,
                    "lastUpdatedAt": 2001,
                    "isDccSupportedByPoc": true,
                    "isDccConsentGiven": true,
                    "isDccDataSetCreated": true,
                    "qrCodeHash": "pcrQrCodeHash"
                }
            ]
        """.toComparableJsonPretty()

        instance.getCoronaTests().single().apply {
            this shouldBe pcrTest1.copy(
                lastError = null,
                isProcessing = false
            )
            type shouldBe BaseCoronaTest.Type.PCR
        }
    }

    @Test
    fun `Store PCRT with isDccSupportedByPoc = false`() = runTest2 {
        val instance = createInstance(this)
        val pcrTest = pcrTest1.copy(_isDccSupportedByPoc = false)
        instance.updateCoronaTests(setOf(pcrTest))

        (dataStore[PKEY_DATA_PCR] as String).toComparableJsonPretty() shouldBe """
            [
                {
                    "identifier": "identifier-pcr1",
                    "registeredAt": 1000,
                    "registrationToken": "regtoken-pcr",
                    "isSubmitted": true,
                    "isViewed": true,
                    "didShowBadge": false,
                    "hasResultChangeBadge":false,
                    "isAdvancedConsentGiven": true,
                    "isResultAvailableNotificationSent": false,
                    "testResultReceivedAt": 2000,
                    "testResult": 2,
                    "lastUpdatedAt": 2001,
                    "isDccSupportedByPoc": false,
                    "isDccConsentGiven": true,
                    "isDccDataSetCreated": true,
                    "qrCodeHash": "pcrQrCodeHash"
                }
            ]
        """.toComparableJsonPretty()

        instance.getCoronaTests().single().apply {
            this shouldBe pcrTest.copy(
                lastError = null,
                isProcessing = false
            )
            type shouldBe BaseCoronaTest.Type.PCR
        }
    }

    @Test
    fun `store only RAT`() = runTest2 {
        val instance = createInstance(this)
        instance.updateCoronaTests(
            setOf(
                raTest.copy(
                    isProcessing = true,
                    lastError = IOException()
                )
            )
        )

        (dataStore[PKEY_DATA_RA] as String).toComparableJsonPretty() shouldBe """
            [
                {
                    "identifier": "identifier-ra",
                    "registeredAt": 1000,
                    "registrationToken": "regtoken-ra",
                    "isSubmitted": true,
                    "isViewed": true,
                    "didShowBadge": false,
                    "hasResultChangeBadge":false,
                    "isAdvancedConsentGiven": true,
                    "isResultAvailableNotificationSent": false,
                    "testResultReceivedAt": 2000,
                    "lastUpdatedAt": 2001,
                    "testResult": 7,
                    "testedAt": 3000,
                    "firstName": "firstname",
                    "lastName": "lastname",
                    "dateOfBirth": "2021-12-24",
                    "isDccSupportedByPoc": true,
                    "isDccConsentGiven": true,
                    "isDccDataSetCreated": true
                }
            ]
        """.toComparableJsonPretty()

        instance.getCoronaTests().single().apply {
            this shouldBe raTest.copy(
                lastError = null,
                isProcessing = false
            )
            type shouldBe BaseCoronaTest.Type.RAPID_ANTIGEN
        }
    }

    @Test
    fun `store only RAT that has QrCodeHash`() = runTest2 {
        val instance = createInstance(this)
        instance.updateCoronaTests(setOf(raTest1))

        (dataStore[PKEY_DATA_RA] as String).toComparableJsonPretty() shouldBe """
            [
                {
                    "identifier": "identifier-ra1",
                    "registeredAt": 1000,
                    "registrationToken": "regtoken-ra",
                    "isSubmitted": true,
                    "isViewed": true,
                    "didShowBadge": false,
                    "hasResultChangeBadge":false,
                    "isAdvancedConsentGiven": true,
                    "isResultAvailableNotificationSent": false,
                    "testResultReceivedAt": 2000,
                    "lastUpdatedAt": 2001,
                    "testResult": 7,
                    "testedAt": 3000,
                    "firstName": "firstname",
                    "lastName": "lastname",
                    "dateOfBirth": "2021-12-24",
                    "isDccSupportedByPoc": true,
                    "isDccConsentGiven": true,
                    "isDccDataSetCreated": true,
                    "qrCodeHash": "raQrCodeHash"
                }
            ]
        """.toComparableJsonPretty()

        instance.getCoronaTests().single().apply {
            this shouldBe raTest1
            type shouldBe BaseCoronaTest.Type.RAPID_ANTIGEN
        }
    }

    @Test
    fun `store one of each`() = runTest2 {
        val instance = createInstance(this)
        instance.updateCoronaTests(setOf(raTest, pcrTest))

        dataStore[PKEY_DATA_RA] shouldNotBe null
        dataStore[PKEY_DATA_PCR] shouldNotBe null

        instance.getCoronaTests() shouldBe setOf(raTest, pcrTest)
    }

    @Test
    fun `storing one and deleting the other`() = runTest2 {
        val instance = createInstance(this)

        instance.updateCoronaTests(setOf(raTest))
        dataStore[PKEY_DATA_RA] shouldNotBe null
        dataStore[PKEY_DATA_PCR] shouldBe null

        instance.updateCoronaTests(setOf(pcrTest))
        dataStore[PKEY_DATA_RA] shouldBe null
        dataStore[PKEY_DATA_PCR] shouldNotBe null
        instance.getCoronaTests() shouldBe setOf(pcrTest)

        instance.updateCoronaTests(setOf(raTest))
        dataStore[PKEY_DATA_RA] shouldNotBe null
        dataStore[PKEY_DATA_PCR] shouldBe null
        instance.getCoronaTests() shouldBe setOf(raTest)
    }
}
