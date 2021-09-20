package de.rki.coronawarnapp.covidcertificate.recovery.core.storage

import android.content.Context
import androidx.core.content.edit
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.RecoveryQrCodeTestData
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.extensions.toComparableJsonPretty
import testhelpers.preferences.MockSharedPreferences

@Suppress("MaxLineLength")
class RecoveryCertificateStorageTest : BaseTest() {
    @MockK lateinit var context: Context
    private lateinit var mockPreferences: MockSharedPreferences
    private val testData = setOf(
        StoredRecoveryCertificateData(
            recoveryCertificateQrCode = RecoveryQrCodeTestData.validRecovery,
            notifiedExpiresSoonAt = null,
            notifiedExpiredAt = null,
            lastSeenStateChange = CwaCovidCertificate.State.Invalid(),
            lastSeenStateChangeAt = Instant.ofEpochMilli(123),
        ),
        StoredRecoveryCertificateData(
            recoveryCertificateQrCode = RecoveryQrCodeTestData.qrCode1,
            notifiedExpiresSoonAt = Instant.ofEpochMilli(876),
            notifiedExpiredAt = Instant.ofEpochMilli(924),
            lastSeenStateChange = CwaCovidCertificate.State.ExpiringSoon(Instant.ofEpochMilli(456)),
            lastSeenStateChangeAt = Instant.ofEpochMilli(123),
        ),
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockPreferences = MockSharedPreferences()

        every {
            context.getSharedPreferences("recovery_localdata", Context.MODE_PRIVATE)
        } returns mockPreferences
    }

    private fun createInstance() = RecoveryCertificateStorage(
        context = context,
        baseGson = SerializationModule().baseGson()
    )

    @Test
    fun `init is sideeffect free`() {
        createInstance()
    }

    @Test
    fun `storing empty set deletes data`() = runBlockingTest {
        mockPreferences.edit {
            putString("dontdeleteme", "test")
            putString("recovery.certificate", "test")
        }
        createInstance().save(emptySet())

        mockPreferences.dataMapPeek.keys.single() shouldBe "dontdeleteme"
    }

    @Test
    fun `store two containers, one for each type`() = runBlockingTest {
        createInstance().save(testData)

        (mockPreferences.dataMapPeek["recovery.certificate"] as String).toComparableJsonPretty() shouldBe """
            [
              {
                "recoveryCertificateQrCode": "${RecoveryQrCodeTestData.validRecovery}",
                "lastSeenStateChange": {
                  "isInvalidSignature": true,
                  "type": "Invalid"
                },
                "lastSeenStateChangeAt": 123,
                "certificateSeenByUser": true
              }, {
                "recoveryCertificateQrCode": "${RecoveryQrCodeTestData.qrCode1}",
                "notifiedExpiresSoonAt": 876,
                "notifiedExpiredAt": 924,
                "lastSeenStateChange": {
                  "expiresAt": 456,
                  "type": "ExpiringSoon"
                },
                "lastSeenStateChangeAt": 123,
                "certificateSeenByUser": true
              }
            ]
        """.toComparableJsonPretty()

        createInstance().load() shouldBe testData
    }
}
