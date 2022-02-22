package de.rki.coronawarnapp.ccl.dccwalletinfo.storage.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.AdmissionState
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.BoosterNotification
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.Certificate
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CertificateRef
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfo
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.OutputCertificates
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.Parameters
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.PluralText
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.QuantityText
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.SingleText
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.VaccinationState
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.Verification
import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseTestInstrumentation
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DccWalletInfoDatabaseTest : BaseTestInstrumentation() {

    private lateinit var dao: DccWalletInfoDao
    private lateinit var db: DccWalletInfoDatabase

    private val mostRelevantCertificate = Certificate(
        certificateRef = CertificateRef(
            barcodeData = "HC1:6..."
        )
    )

    private val verification = Verification(
        certificates = listOf(
            OutputCertificates(
                buttonText = SingleText(
                    type = "string",
                    localizedText = mapOf("de" to "2G-Zertifikat"),
                    parameters = listOf()
                ),
                certificateRef = CertificateRef(
                    barcodeData = "HC1:6..."
                )
            ),

            OutputCertificates(
                buttonText = SingleText(
                    type = "string",
                    localizedText = mapOf("de" to "Testzertifikat"),
                    parameters = listOf()
                ),
                certificateRef = CertificateRef(
                    barcodeData = "HC1:6..."
                )
            )
        )
    )

    private val vaccinationState = VaccinationState(
        visible = true,
        titleText = SingleText(
            type = "string",
            localizedText = mapOf("de" to "Impfstatus"),
            parameters = listOf()
        ),
        subtitleText = PluralText(
            type = "plural",
            quantity = 25,
            localizedText = mapOf(
                "de" to QuantityText(
                    zero = "Letzte Impfung heute",
                    one = "Letzte Impfung vor %u Tag",
                    two = "Letzte Impfung vor %u Tagen",
                    few = "Letzte Impfung vor %u Tagen",
                    many = "Letzte Impfung vor %u Tagen",
                    other = "Letzte Impfung vor %u Tagen"
                )
            ),
            parameters = listOf(
                Parameters(
                    type = Parameters.Type.LOCAL_DATE,
                    value = "2022-01-01T23:30:00.000Z"
                )
            )
        ),
        longText = SingleText(
            type = "string",
            localizedText = mapOf("de" to "Sie haben nun alle derzeit [...]"),
            parameters = listOf()
        ),
        faqAnchor = "dcc_admission_state"
    )

    private val admissionState = AdmissionState(
        visible = true,
        badgeText = SingleText(
            type = "string",
            localizedText = mapOf("de" to "2G+"),
            parameters = listOf()
        ),
        titleText = SingleText(
            type = "string",
            localizedText = mapOf("de" to "Status-Nachweis"),
            parameters = listOf()
        ),
        subtitleText = SingleText(
            type = "string",
            localizedText = mapOf("de" to "2G+ PCR-Test"),
            parameters = listOf()
        ),
        longText = SingleText(
            type = "string",
            localizedText = mapOf("de" to "Ihre Zertifikate erfüllen [...]"),
            parameters = listOf()
        ),
        faqAnchor = "dcc_admission_state"
    )

    private val boosterNotification = BoosterNotification(
        visible = true,
        titleText = SingleText(
            type = "string",
            localizedText = mapOf("de" to "Booster"),
            parameters = listOf()
        ),
        subtitleText = SingleText(
            type = "string",
            localizedText = mapOf("de" to "Empfehlung einer Booster-Impfung"),
            parameters = listOf()
        ),
        longText = SingleText(
            type = "string",
            localizedText = mapOf("de" to "Die Ständige Impfkommission (STIKO) empfiehlt allen Personen [...]"),
            parameters = listOf()
        ),
        faqAnchor = "dcc_admission_state",
        identifier = "booster_rule_identifier"
    )

    private val dccWalletInfo = DccWalletInfo(
        admissionState = admissionState,
        vaccinationState = vaccinationState,
        verification = verification,
        boosterNotification = boosterNotification,
        mostRelevantCertificate = mostRelevantCertificate,
        validUntil = "2022-01-14T18:43:00Z"
    )

    private val personIdentifier = CertificatePersonIdentifier(
        firstNameStandardized = "Erika",
        lastNameStandardized = "MusterFrau",
        dateOfBirthFormatted = "1980-01-01"
    )

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, DccWalletInfoDatabase::class.java
        ).build()
        dao = db.dccWalletInfoDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeDccWalletInfoAndReadInList() = runBlocking {
        val personId = personIdentifier.groupingKey
        val personWallet = DccWalletInfoEntity(personId, dccWalletInfo)

        dao.insert(personWallet)
        dao.getAll().first()[0] shouldBe personWallet
    }

    @Test
    @Throws(Exception::class)
    fun deleteAll() = runBlocking {
        val personId = personIdentifier.groupingKey
        val personWallet = DccWalletInfoEntity(personId, dccWalletInfo)

        dao.insert(personWallet)
        dao.deleteAll()
        dao.getAll().first() shouldBe listOf()
    }

    @Test
    @Throws(Exception::class)
    fun deleteEntity() = runBlocking {
        val personId = personIdentifier.groupingKey
        val personWallet = DccWalletInfoEntity(personId, dccWalletInfo)

        dao.insert(personWallet)
        dao.delete(personWallet)
        dao.getAll().first() shouldBe listOf()
    }

    @Test
    @Throws(Exception::class)
    fun deleteBy() = runBlocking {
        val personId = personIdentifier.groupingKey
        val personWallet = DccWalletInfoEntity(personId, dccWalletInfo)

        dao.insert(personWallet)
        dao.deleteBy(setOf(personId))
        dao.getAll().first() shouldBe listOf()
    }
}
