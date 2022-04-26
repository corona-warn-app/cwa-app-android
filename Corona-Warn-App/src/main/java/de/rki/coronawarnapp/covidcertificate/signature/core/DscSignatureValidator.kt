package de.rki.coronawarnapp.covidcertificate.signature.core

import dagger.Reusable
import de.rki.coronawarnapp.SecurityProvider
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DscMessage
import de.rki.coronawarnapp.covidcertificate.common.certificate.DscMessage.Algorithm.ES256
import de.rki.coronawarnapp.covidcertificate.common.certificate.DscMessage.Algorithm.PS256
import de.rki.coronawarnapp.covidcertificate.common.certificate.RecoveryDccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.TestDccV1
import de.rki.coronawarnapp.covidcertificate.common.certificate.VaccinationDccV1
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_DSC_EXPIRED
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_DSC_NOT_YET_VALID
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_DSC_NO_MATCH
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_DSC_OID_MISMATCH_RC
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_DSC_OID_MISMATCH_TC
import de.rki.coronawarnapp.covidcertificate.common.exception.InvalidHealthCertificateException.ErrorCode.HC_DSC_OID_MISMATCH_VC
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.security.PublicKey
import java.security.Signature
import java.security.cert.CertificateExpiredException
import java.security.cert.CertificateFactory
import java.security.cert.CertificateNotYetValidException
import java.security.cert.X509Certificate
import java.util.Date
import javax.inject.Inject

/**
 * Security provider is added by [SecurityProvider] at app start
 */
@Reusable
class DscSignatureValidator @Inject constructor(
    securityProvider: SecurityProvider,
    private val dscRepository: DscRepository
) {
    private val certificateFactory by lazy {
        CertificateFactory.getInstance("X.509")
    }

    init {
        securityProvider.setup()
    }

    private val vcOids = setOf(
        "1.3.6.1.4.1.1847.2021.1.2",
        "1.3.6.1.4.1.0.1847.2021.1.2"
    )
    private val tcOids = setOf(
        "1.3.6.1.4.1.1847.2021.1.1",
        "1.3.6.1.4.1.0.1847.2021.1.1"
    )
    private val rcOids = setOf(
        "1.3.6.1.4.1.1847.2021.1.3",
        "1.3.6.1.4.1.0.1847.2021.1.3"
    )

    private val oidSet = vcOids + tcOids + rcOids

    /**
     * @throws InvalidHealthCertificateException if validation fail, otherwise it is OK!
     */
    suspend fun validateSignature(
        dccData: DccData<*>,
        preFetchedDscData: DscSignatureList? = null,
        date: Date = Date()
    ) {
        val dscData = preFetchedDscData ?: dscRepository.dscSignatureList.first()
        findDscCertificate(dscData, dccData.dscMessage).apply {
            validate(date)
            checkOidsIntersect(dccData)
        }
    }

    private fun findDscCertificate(
        dscData: DscSignatureList,
        dscMessage: DscMessage
    ): X509Certificate {
        val toVerify = dscMessage.signedPayload()
        val filteredDscSet = dscData.dscList.filter { it.kid == dscMessage.kid }
        val matchedDscSet = when {
            filteredDscSet.isEmpty() || dscMessage.kid.isEmpty() -> dscData.dscList
            else -> filteredDscSet
        }

        Timber.tag(TAG).d(
            "validateSignature() dscListSize=%d, filteredDscSetSize=%d, matchedDscSetSize=%d",
            dscData.dscList.size,
            filteredDscSet.size,
            matchedDscSet.size
        )

        var x509Certificate: X509Certificate? = null
        val exceptionList = mutableListOf<String>()
        for (dsc in matchedDscSet) {
            try {
                val dscCertificate = dsc.toX509certificate()
                val (publicKey, signature) = when (dscMessage.algorithm) {
                    ES256 -> dscCertificate.publicKey to dscMessage.signature.toByteArray().toECDSAVerifier()
                    PS256 -> dscCertificate.publicKey.toRsaPublicKey() to dscMessage.signature.toByteArray()
                }
                val valid = Signature.getInstance(dscMessage.algorithm.algName).verify(publicKey, toVerify, signature)
                if (valid) {
                    x509Certificate = dscCertificate
                    break
                }
            } catch (e: Exception) {
                e.message?.let { exceptionList.add(it) }
            }
        }

        if (exceptionList.isNotEmpty()) {
            Timber.w("Signature verification exceptions: %s", exceptionList.distinct().joinToString())
        }

        return x509Certificate ?: throw InvalidHealthCertificateException(HC_DSC_NO_MATCH)
    }

    private fun DscItem.toX509certificate(): X509Certificate =
        data.toByteArray().inputStream().use {
            certificateFactory.generateCertificate(it) as X509Certificate
        }

    private fun X509Certificate.validate(date: Date) {
        try {
            checkValidity(date)
        } catch (e: CertificateExpiredException) {
            throw InvalidHealthCertificateException(HC_DSC_EXPIRED)
        } catch (e: CertificateNotYetValidException) {
            throw InvalidHealthCertificateException(HC_DSC_NOT_YET_VALID)
        }
    }

    private fun X509Certificate.checkOidsIntersect(dccData: DccData<*>) {
        val extendedKeysIntersect = extendedKeyUsage.orEmpty().toSet() intersect oidSet
        if (extendedKeysIntersect.isEmpty()) return // OK!
        when (dccData.certificate) {
            is VaccinationDccV1 -> if (vcOids.intersect(extendedKeysIntersect).isEmpty())
                throw InvalidHealthCertificateException(HC_DSC_OID_MISMATCH_VC)

            is TestDccV1 -> if (tcOids.intersect(extendedKeysIntersect).isEmpty())
                throw InvalidHealthCertificateException(HC_DSC_OID_MISMATCH_TC)

            is RecoveryDccV1 -> if (rcOids.intersect(extendedKeysIntersect).isEmpty())
                throw InvalidHealthCertificateException(HC_DSC_OID_MISMATCH_RC)
        }
    }

    private fun Signature.verify(
        verificationKey: PublicKey,
        toVerify: ByteArray,
        signature: ByteArray
    ): Boolean {
        initVerify(verificationKey)
        update(toVerify)
        return verify(signature)
    }

    companion object {
        private const val TAG = "DscSignatureValidator"
    }
}
