package de.rki.coronawarnapp.covidcertificate.test

import com.google.gson.Gson
import com.upokecenter.cbor.CBORObject
import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.exception.InvalidHealthCertificateException.ErrorCode.HC_CBOR_DECODING_FAILED
import de.rki.coronawarnapp.covidcertificate.exception.InvalidHealthCertificateException.ErrorCode.HC_CWT_NO_DGC
import de.rki.coronawarnapp.covidcertificate.exception.InvalidHealthCertificateException.ErrorCode.HC_CWT_NO_HCERT
import de.rki.coronawarnapp.covidcertificate.exception.InvalidHealthCertificateException.ErrorCode.JSON_SCHEMA_INVALID
import de.rki.coronawarnapp.covidcertificate.exception.InvalidHealthCertificateException.ErrorCode.NO_TEST_ENTRY
import de.rki.coronawarnapp.covidcertificate.exception.InvalidTestCertificateException
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.serialization.fromJson
import timber.log.Timber
import javax.inject.Inject

@Reusable
class TestCertificateDccParser @Inject constructor(
    @BaseGson private val gson: Gson,
) {
    fun parse(map: CBORObject): TestCertificateDccV1 = try {
        val certificate: TestCertificateDccV1 = map[keyHCert]?.run {
            this[keyEuDgcV1]?.run {
                toCertificate()
            } ?: throw InvalidTestCertificateException(HC_CWT_NO_DGC)
        } ?: throw InvalidTestCertificateException(HC_CWT_NO_HCERT)
        certificate
    } catch (e: InvalidTestCertificateException) {
        throw e
    } catch (e: Throwable) {
        throw InvalidTestCertificateException(HC_CBOR_DECODING_FAILED)
    }

    @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
    private fun TestCertificateDccV1.validate(): TestCertificateDccV1 {
        if (testCertificateData.isNullOrEmpty()) {
            throw InvalidTestCertificateException(NO_TEST_ENTRY)
        }
        // check for non null & force date parsing
        nameData.familyNameStandardized!!
        dateOfBirth
        testCertificateData.forEach {
            it.testResultAt
            it.sampleCollectedAt
            it.certificateIssuer!!
            it.countryOfTest!!
            it.targetId!!
            it.testCenter!!
            it.testResult!!
            it.testType!!
        }
        return this
    }

    private fun CBORObject.toCertificate() = try {
        val json = ToJSONString()
        gson.fromJson<TestCertificateDccV1>(json).validate()
    } catch (e: InvalidTestCertificateException) {
        throw e
    } catch (e: Throwable) {
        Timber.e(e)
        throw InvalidTestCertificateException(JSON_SCHEMA_INVALID)
    }

    companion object {
        private val keyEuDgcV1 = CBORObject.FromObject(1)
        private val keyHCert = CBORObject.FromObject(-260)
    }
}
