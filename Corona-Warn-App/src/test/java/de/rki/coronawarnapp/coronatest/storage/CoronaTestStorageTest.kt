package de.rki.coronawarnapp.coronatest.storage

import android.content.Context
import androidx.core.content.edit
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.extensions.toComparableJsonPretty
import testhelpers.preferences.MockSharedPreferences
import java.io.IOException

class CoronaTestStorageTest : BaseTest() {
    @MockK lateinit var context: Context
    private lateinit var mockPreferences: MockSharedPreferences

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockPreferences = MockSharedPreferences()

        every {
            context.getSharedPreferences("coronatest_localdata", Context.MODE_PRIVATE)
        } returns mockPreferences
    }

    private fun createInstance() = CoronaTestStorage(
        context = context,
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
    fun `init is sideeffect free`() {
        createInstance()
    }

    @Test
    fun `storing empty set deletes data`() {
        mockPreferences.edit {
            putString("dontdeleteme", "test")
            putString("coronatest.data.ra", "test")
            putString("coronatest.data.pcr", "test")
        }
        createInstance().coronaTests = emptySet()

        mockPreferences.dataMapPeek.keys.single() shouldBe "dontdeleteme"
    }

    @Test
    fun `store only PCRT`() {
        val instance = createInstance()
        instance.coronaTests = setOf(
            pcrTest.copy(
                isProcessing = true,
                lastError = IOException()
            )
        )

        val json = (mockPreferences.dataMapPeek["coronatest.data.pcr"] as String)

        json.toComparableJsonPretty() shouldBe """
            [
                {
                    "identifier": "identifier-pcr",
                    "registeredAt": 1000,
                    "registrationToken": "regtoken-pcr",
                    "isSubmitted": true,
                    "isViewed": true,
                     "didShowBadge": false,
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

        instance.coronaTests.single().apply {
            this shouldBe pcrTest.copy(
                lastError = null,
                isProcessing = false
            )
            type shouldBe BaseCoronaTest.Type.PCR
        }
    }

    @Test
    fun `store only PCRT that has QrCode Hash`() {
        val instance = createInstance()
        instance.coronaTests = setOf(pcrTest1)

        val json = (mockPreferences.dataMapPeek["coronatest.data.pcr"] as String)

        json.toComparableJsonPretty() shouldBe """
            [
                {
                    "identifier": "identifier-pcr1",
                    "registeredAt": 1000,
                    "registrationToken": "regtoken-pcr",
                    "isSubmitted": true,
                    "isViewed": true,
                     "didShowBadge": false,
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

        instance.coronaTests.single().apply {
            this shouldBe pcrTest1.copy(
                lastError = null,
                isProcessing = false
            )
            type shouldBe BaseCoronaTest.Type.PCR
        }
    }

    @Test
    fun `Store PCRT with isDccSupportedByPoc = false`() {
        val instance = createInstance()
        val test = pcrTest1.copy(_isDccSupportedByPoc = false)
        instance.coronaTests = setOf(test)

        val json = (mockPreferences.dataMapPeek["coronatest.data.pcr"] as String)

        json.toComparableJsonPretty() shouldBe """
            [
                {
                    "identifier": "identifier-pcr1",
                    "registeredAt": 1000,
                    "registrationToken": "regtoken-pcr",
                    "isSubmitted": true,
                    "isViewed": true,
                     "didShowBadge": false,
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

        instance.coronaTests.single().apply {
            this shouldBe test.copy(
                lastError = null,
                isProcessing = false
            )
            type shouldBe BaseCoronaTest.Type.PCR
        }
    }

    @Test
    fun `store only RAT`() {
        val instance = createInstance()
        instance.coronaTests = setOf(
            raTest.copy(
                isProcessing = true,
                lastError = IOException()
            )
        )

        val json = (mockPreferences.dataMapPeek["coronatest.data.ra"] as String)

        json.toComparableJsonPretty() shouldBe """
            [
                {
                    "identifier": "identifier-ra",
                    "registeredAt": 1000,
                    "registrationToken": "regtoken-ra",
                    "isSubmitted": true,
                    "isViewed": true,
                    "didShowBadge": false,
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

        instance.coronaTests.single().apply {
            this shouldBe raTest.copy(
                lastError = null,
                isProcessing = false
            )
            type shouldBe BaseCoronaTest.Type.RAPID_ANTIGEN
        }
    }

    @Test
    fun `store only RAT that has QrCodeHash`() {
        val instance = createInstance()
        instance.coronaTests = setOf(raTest1)

        val json = (mockPreferences.dataMapPeek["coronatest.data.ra"] as String)

        json.toComparableJsonPretty() shouldBe """
            [
                {
                    "identifier": "identifier-ra1",
                    "registeredAt": 1000,
                    "registrationToken": "regtoken-ra",
                    "isSubmitted": true,
                    "isViewed": true,
                    "didShowBadge": false,
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

        instance.coronaTests.single().apply {
            this shouldBe raTest1
            type shouldBe BaseCoronaTest.Type.RAPID_ANTIGEN
        }
    }

    @Test
    fun `store one of each`() {
        val instance = createInstance()
        instance.coronaTests = setOf(raTest, pcrTest)

        mockPreferences.contains("coronatest.data.ra") shouldBe true
        mockPreferences.contains("coronatest.data.pcr") shouldBe true

        instance.coronaTests shouldBe setOf(raTest, pcrTest)
    }

    @Test
    fun `storing one and deleting the other`() {
        val instance = createInstance()

        instance.coronaTests = setOf(raTest)
        mockPreferences.contains("coronatest.data.ra") shouldBe true
        mockPreferences.contains("coronatest.data.pcr") shouldBe false

        instance.coronaTests = setOf(pcrTest)

        mockPreferences.contains("coronatest.data.ra") shouldBe false
        mockPreferences.contains("coronatest.data.pcr") shouldBe true
        instance.coronaTests shouldBe setOf(pcrTest)

        instance.coronaTests = setOf(raTest)

        mockPreferences.contains("coronatest.data.ra") shouldBe true
        mockPreferences.contains("coronatest.data.pcr") shouldBe false
        instance.coronaTests shouldBe setOf(raTest)
    }
}
