package de.rki.coronawarnapp.covidcertificate.test.core.server

data class TestCertificateComponents(
    val dataEncryptionKeyBase64: String,
    val encryptedCoseTestCertificateBase64: String,
)
