package de.rki.coronawarnapp.vaccination.core.qrcode

import com.google.gson.Gson
import com.upokecenter.cbor.CBORObject
import de.rki.coronawarnapp.util.serialization.fromJson
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.HC_CBOR_DECODING_FAILED
import de.rki.coronawarnapp.vaccination.core.qrcode.InvalidHealthCertificateException.ErrorCode.VC_NO_VACCINATION_ENTRY
import org.joda.time.Instant
import javax.inject.Inject

class VaccinationCertificateV1Parser @Inject constructor() {

    private val keyEuDgcV1 = CBORObject.FromObject(1)
    private val keyHCert = CBORObject.FromObject(-260)
    private val keyIssuer = CBORObject.FromObject(1)
    private val keyExpiresAt = CBORObject.FromObject(4)
    private val keyIssuedAt = CBORObject.FromObject(6)

    fun decode(map: CBORObject): VaccinationCertificateData {
        try {
            var issuer: String? = null
            map[keyIssuer]?.let {
                issuer = it.AsString()
            }
            var issuedAt: Instant? = null
            map[keyIssuedAt]?.let {
                issuedAt = Instant.ofEpochSecond(it.AsNumber().ToInt64Checked())
            }
            var expiresAt: Instant? = null
            map[keyExpiresAt]?.let {
                expiresAt = Instant.ofEpochSecond(it.AsNumber().ToInt64Checked())
            }
            var certificate: VaccinationCertificateV1? = null
            map[keyHCert]?.let { hcert ->
                hcert[keyEuDgcV1]?.let {
                    val json = it.ToJSONString()
                    certificate = Gson().fromJson<VaccinationCertificateV1>(json)
                }
            }
            val header = VaccinationCertificateHeader(
                issuer = issuer!!,
                issuedAt = issuedAt!!,
                expiresAt = expiresAt!!
            )
            return VaccinationCertificateData(
                header,
                certificate!!.validate()
            )
        } catch (e: InvalidHealthCertificateException) {
            throw e
        } catch (e: Throwable) {
            throw InvalidHealthCertificateException(HC_CBOR_DECODING_FAILED)
        }
    }

    private fun VaccinationCertificateV1.validate(): VaccinationCertificateV1 {
        if (vaccinationDatas.isEmpty()) {
            throw InvalidHealthCertificateException(VC_NO_VACCINATION_ENTRY)
        }
        dateOfBirth
        vaccinationDatas.forEach {
            it.vaccinatedAt
        }
        return this
    }
}

data class VaccinationCertificateHeader(
    val issuer: String,
    val issuedAt: Instant,
    val expiresAt: Instant
)
