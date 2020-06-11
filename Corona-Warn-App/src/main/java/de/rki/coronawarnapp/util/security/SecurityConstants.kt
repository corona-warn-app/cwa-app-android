package de.rki.coronawarnapp.util.security

object SecurityConstants {
    const val DIGEST_ALGORITHM = "SHA-256"
    const val DB_PASSWORD_MIN_LENGTH = 32
    const val DB_PASSWORD_MAX_LENGTH = 48
    const val CWA_APP_SQLITE_DB_PW = "CWA_APP_SQLITE_DB_PW"
    const val ENCRYPTED_SHARED_PREFERENCES_FILE = "shared_preferences_cwa"
    const val EXPORT_FILE_SIGNATURE_VERIFICATION_ALGORITHM = "SHA256withECDSA"
}
