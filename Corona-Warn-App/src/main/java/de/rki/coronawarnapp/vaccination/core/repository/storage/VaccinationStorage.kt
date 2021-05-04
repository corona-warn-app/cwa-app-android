package de.rki.coronawarnapp.vaccination.core.repository.storage

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
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
        // Allow for custom type adapter.
        baseGson
    }

    var personContainers: Set<PersonData>
        get() {
            Timber.tag(TAG).d("vaccinatedPersons - load()")
            val persons = prefs.all.mapNotNull { (key, value) ->
                if (!key.startsWith(PKEY_PERSON_PREFIX)) {
                    return@mapNotNull null
                }
                value as String
                gson.fromJson<PersonData>(value).also {
                    Timber.tag(TAG).v("Person loaded: %s", it)
                    requireNotNull(it.identifier)
                }
            }
            return persons.toSet()
        }
        set(persons) {
            Timber.tag(TAG).d("vaccinatedPersons - save(%s)", persons)

            prefs.edit {
                prefs.all.keys.filter { it.startsWith(PKEY_PERSON_PREFIX) }.forEach {
                    Timber.tag(TAG).v("Removing data for %s", it)
                    remove(it)
                }
                persons.forEach {
                    val raw = gson.toJson(it)
                    val identifier = it.identifier
                    Timber.tag(TAG).v("Storing vaccinatedPerson %s -> %s", identifier, raw)
                    putString("$PKEY_PERSON_PREFIX${identifier.code}", raw)
                }

            }
        }

    init {
        if (CWADebug.isDeviceForTestersBuild && !CWADebug.isAUnitTest) {
            val completePersonCert1 = VaccinationContainer.StoredCertificate(
                firstName = "François-Joan",
                firstNameStandardized = "FRANCOIS<JOAN",
                lastName = "d'Arsøns - van Halen",
                lastNameStandardized = "DARSONS<VAN<HALEN",
                dateOfBirth = LocalDate.parse("2009-02-28"),
                targetId = "840539006",
                vaccineId = "1119349007",
                medicalProductId = "EU/1/20/1528",
                marketAuthorizationHolderId = "ORG-100030215",
                doseNumber = 1,
                totalSeriesOfDoses = 2,
                vaccinatedAt = LocalDate.parse("2021-04-21"),
                certificateCountryCode = "NL",
                certificateIssuer = "Ministry of Public Health, Welfare and Sport",
                certificateId = "urn:uvci:01:NL:PlA8UWS60Z4RZXVALl6GAZ",
                lotNumber = "0020617",
            )

            val completePersonCert2 = completePersonCert1.copy(
                doseNumber = 2,
                vaccinatedAt = completePersonCert1.vaccinatedAt.plusDays(1)
            )

            val completePerson = PersonData(
                vaccinations = setOf(
                    VaccinationContainer(
                        certificate = completePersonCert1,
                        scannedAt = Instant.ofEpochMilli(1620062834471),
                        certificateBase45 = "BASE45",
                        certificateCBORBase64 = "BASE64"
                    ),
                    VaccinationContainer(
                        certificate = completePersonCert2,
                        scannedAt = Instant.ofEpochMilli(1620149234473),
                        certificateBase45 = "BASE45",
                        certificateCBORBase64 = "BASE64"
                    )
                ),
                proofs = setOf(
                    ProofContainer(
                        proof = ProofContainer.StoredProof(
                            identifier = "some-identifier"
                        ),
                        expiresAt = Instant.ofEpochMilli(1620322034474),
                        updatedAt = Instant.ofEpochMilli(1620062834474),
                        proofCBORBase64 = "BASE64",
                    )
                ),
            )
            if (personContainers.isEmpty()) {
                personContainers = setOf(completePerson)
            }
        }
    }

    companion object {
        private const val TAG = "VaccinationStorage"
        private const val PKEY_PERSON_PREFIX = "vaccination.person."
    }
}
