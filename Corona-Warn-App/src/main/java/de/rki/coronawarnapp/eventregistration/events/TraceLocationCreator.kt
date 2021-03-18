package de.rki.coronawarnapp.eventregistration.events

import dagger.Lazy
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.toTraceLocation
import de.rki.coronawarnapp.eventregistration.events.server.TraceLocationServer
import de.rki.coronawarnapp.eventregistration.storage.repo.TraceLocationRepository
import de.rki.coronawarnapp.util.security.SignatureValidation
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

const val TRACE_LOCATION_VERSION = 1

@Singleton
class TraceLocationCreator @Inject constructor(
    private val api: Lazy<TraceLocationServer>,
    private val repository: TraceLocationRepository,
    private val signatureValidation: SignatureValidation
) {

    suspend fun createTraceLocation(traceLocationUserInput: TraceLocationUserInput): TraceLocation {

        val signedTraceLocation = api.get().retrieveSignedTraceLocation(traceLocationUserInput)

        val isSignatureValid = try {
            signatureValidation.hasValidSignature(
                signedTraceLocation.location.toByteArray(),
                sequenceOf(signedTraceLocation.signature.toByteArray())
            )
        } catch (exception: Exception) {
            Timber.e("Signature Validation Failed: $exception")
            throw SignatureValidationFailedException("Signature Validation failed", exception)
        }

        if (!isSignatureValid) {
            Timber.e("The received trace location has an invalid signature")
            throw InvalidSignatureException("The received trace location has an invalid signature", null)
        }

        val traceLocation = signedTraceLocation.toTraceLocation()

        repository.addTraceLocation(traceLocation)

        return traceLocation
    }
}

// TODO: Finalize Error Handling in a future PR when it was specified in TechSpecs
class InvalidSignatureException(message: String?, cause: Throwable?) : Exception(message, cause)
class SignatureValidationFailedException(message: String?, cause: Throwable?) : Exception(message, cause)
