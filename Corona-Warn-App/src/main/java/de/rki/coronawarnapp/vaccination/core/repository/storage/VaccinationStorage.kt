package de.rki.coronawarnapp.vaccination.core.repository.storage

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.vaccination.core.VaccinatedPersonIdentifier
import de.rki.coronawarnapp.vaccination.core.qrcode.VaccinationCertificateV1
import okio.ByteString.Companion.decodeHex
import org.joda.time.Duration
import org.joda.time.Instant
import org.joda.time.LocalDate
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaccinationStorage @Inject constructor(
    @AppContext val context: Context,
    @BaseGson val baseGson: Gson
) {

    private val prefs by lazy {
        context.getSharedPreferences("vaccination_localdata", Context.MODE_PRIVATE)
    }

    private val gson by lazy {
        baseGson.newBuilder().apply {
//            registerTypeAdapter(VaccinatedPerson::class.java, CoronaTestResult.GsonAdapter())
        }.create()
    }

    private val typeTokenVaccinatedPerson by lazy {
        object : TypeToken<Set<VaccinatedPerson>>() {}.type
    }

    var vaccinatedPersons: Collection<VaccinatedPerson>
        get() {
            Timber.tag(TAG).d("vaccinatedPersons - load()")

            val raw = prefs.getString(PKEY_VACCINATED_PERSON, null) ?: return emptySet()

            val persons = gson.fromJson<Set<VaccinatedPerson>>(raw, typeTokenVaccinatedPerson).onEach {
                Timber.tag(TAG).v("PCR loaded: %s", it)
                requireNotNull(it.identifier)
            }

            Timber.tag(TAG).v("Loaded %d values: %s", persons.size, persons)
            return persons
        }
        set(value) {
            Timber.tag(TAG).d("vaccinatedPersons - save(%s)", value)

            prefs.edit {
                if (value.isNotEmpty()) {
                    val raw = gson.toJson(this, typeTokenVaccinatedPerson)
                    Timber.tag(TAG).v("Storing vaccinatedPerson data, raw: %s", raw)
                    putString(PKEY_VACCINATED_PERSON, raw)
                } else {
                    Timber.tag(TAG).v("vaccinatedPersons is empty, clearing.")
                    remove(PKEY_VACCINATED_PERSON)
                }
            }
        }

    var proofContainer: Map<VaccinatedPersonIdentifier, Set<ProofContainer>>

    var vaccinationContainer: Map<VaccinatedPersonIdentifier, Set<VaccinationContainer>>

    companion object {
        private const val TAG = "VaccinationStorage"
        private const val PKEY_VACCINATED_PERSON = "vaccination.vaccinatedperson"

        private val testVacCert1 = VaccinationCertificateV1(
            version = "1.0.0",
            nameData = VaccinationCertificateV1.NameData(
                givenName = "François-Joan",
                givenNameStandardized = "FRANCOIS<JOAN",
                familyName = "d'Arsøns - van Halen",
                familyNameStandardized = "DARSONS<VAN<HALEN",
            ),
            dateOfBirth = LocalDate.parse("2009-02-28"),
            vaccinationData = VaccinationCertificateV1.VaccinationData(
                targetId = "840539006",
                vaccineId = "1119349007",
                medicalProductId = "EU/1/20/1528",
                markedAuthorizationHolder = "ORG-100030215",
                doseNumber = 1,
                totalSeriesOfDoses = 2,
                vaccinatedAt = LocalDate.parse("2021-04-21"),
                countryOfVaccination = "NL",
                certificateIssuer = "Ministry of Public Health, Welfare and Sport",
                uniqueCertificateIdentifier = "urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ"
            )
        )

        private val testVacCert2 = VaccinationCertificateV1(
            version = "1.0.0",
            nameData = VaccinationCertificateV1.NameData(
                givenName = "François-Joan",
                givenNameStandardized = "FRANCOIS<JOAN",
                familyName = "d'Arsøns - van Halen",
                familyNameStandardized = "DARSONS<VAN<HALEN",
            ),
            dateOfBirth = LocalDate.parse("2009-02-28"),
            vaccinationData = VaccinationCertificateV1.VaccinationData(
                targetId = "840539006",
                vaccineId = "1119349007",
                medicalProductId = "EU/1/20/1528",
                markedAuthorizationHolder = "ORG-100030215",
                doseNumber = 2,
                totalSeriesOfDoses = 2,
                vaccinatedAt = LocalDate.parse("2021-04-21"),
                countryOfVaccination = "NL",
                certificateIssuer = "Ministry of Public Health, Welfare and Sport",
                uniqueCertificateIdentifier = "urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ"
            )
        )

        private val testData = setOf(
            VaccinatedPerson(
                vaccinationCertificates = setOf(
                    VaccinationContainer(
                        certificate = testVacCert1,
                        scannedAt = Instant.now(),
                        qrCodeOriginalBase45 = "",
                        qrCodeOriginalCBOR = "".decodeHex()
                    ),
                    VaccinationContainer(
                        certificate = testVacCert2,
                        scannedAt = Instant.now(),
                        qrCodeOriginalBase45 = "",
                        qrCodeOriginalCBOR = "".decodeHex()
                    )
                ),
                proofCertificates = setOf(
                    ProofContainer(
                        expiresAt = Instant.now().plus(Duration.standardDays(3)),
                        updatedAt = Instant.now()
                    )
                ),
                isUpdatingData = false,
                lastError = null,
                valueSet = TODO(),
            )
        )
    }
}
