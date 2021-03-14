package de.rki.coronawarnapp.http

import de.rki.coronawarnapp.exception.http.BadGatewayException
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.exception.http.ConflictException
import de.rki.coronawarnapp.exception.http.CwaClientError
import de.rki.coronawarnapp.exception.http.CwaInformationalNotSupportedError
import de.rki.coronawarnapp.exception.http.CwaRedirectNotSupportedError
import de.rki.coronawarnapp.exception.http.CwaServerError
import de.rki.coronawarnapp.exception.http.CwaSuccessResponseWithCodeMismatchNotSupportedError
import de.rki.coronawarnapp.exception.http.CwaUnknownHostException
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.exception.http.ForbiddenException
import de.rki.coronawarnapp.exception.http.GatewayTimeoutException
import de.rki.coronawarnapp.exception.http.GoneException
import de.rki.coronawarnapp.exception.http.HTTPVersionNotSupported
import de.rki.coronawarnapp.exception.http.InternalServerErrorException
import de.rki.coronawarnapp.exception.http.NetworkAuthenticationRequiredException
import de.rki.coronawarnapp.exception.http.NetworkConnectTimeoutException
import de.rki.coronawarnapp.exception.http.NetworkReadTimeoutException
import de.rki.coronawarnapp.exception.http.NotFoundException
import de.rki.coronawarnapp.exception.http.NotImplementedException
import de.rki.coronawarnapp.exception.http.ServiceUnavailableException
import de.rki.coronawarnapp.exception.http.TooManyRequestsException
import de.rki.coronawarnapp.exception.http.UnauthorizedException
import de.rki.coronawarnapp.exception.http.UnsupportedMediaTypeException
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.HttpsURLConnection.HTTP_ACCEPTED
import javax.net.ssl.HttpsURLConnection.HTTP_BAD_GATEWAY
import javax.net.ssl.HttpsURLConnection.HTTP_BAD_REQUEST
import javax.net.ssl.HttpsURLConnection.HTTP_CONFLICT
import javax.net.ssl.HttpsURLConnection.HTTP_CREATED
import javax.net.ssl.HttpsURLConnection.HTTP_FORBIDDEN
import javax.net.ssl.HttpsURLConnection.HTTP_GATEWAY_TIMEOUT
import javax.net.ssl.HttpsURLConnection.HTTP_GONE
import javax.net.ssl.HttpsURLConnection.HTTP_INTERNAL_ERROR
import javax.net.ssl.HttpsURLConnection.HTTP_NOT_FOUND
import javax.net.ssl.HttpsURLConnection.HTTP_NOT_IMPLEMENTED
import javax.net.ssl.HttpsURLConnection.HTTP_NO_CONTENT
import javax.net.ssl.HttpsURLConnection.HTTP_OK
import javax.net.ssl.HttpsURLConnection.HTTP_UNAUTHORIZED
import javax.net.ssl.HttpsURLConnection.HTTP_UNAVAILABLE
import javax.net.ssl.HttpsURLConnection.HTTP_UNSUPPORTED_TYPE
import javax.net.ssl.HttpsURLConnection.HTTP_VERSION

class HttpErrorParser : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            val response = chain.proceed(chain.request())

            if (response.isSuccessful) {
                return response
            }

            val statusMessage: String? = try {
                response.message
            } catch (e: Exception) {
                Timber.w("Failed to get http status-message.")
                null
            }

            val body: String? = try {
                response.peekBody(2048).string()
            } catch (e: Exception) {
                Timber.w("Failed to get http error body.")
                null
            }

            val errorDetails = "$statusMessage body=$body'"

            return when (val code = response.code) {
                HTTP_OK -> response
                HTTP_CREATED -> response
                HTTP_ACCEPTED -> response
                HTTP_NO_CONTENT -> response
                HTTP_BAD_REQUEST -> throw BadRequestException(errorDetails)
                HTTP_UNAUTHORIZED -> throw UnauthorizedException(errorDetails)
                HTTP_FORBIDDEN -> throw ForbiddenException(errorDetails)
                HTTP_NOT_FOUND -> throw NotFoundException(errorDetails)
                HTTP_CONFLICT -> throw ConflictException(errorDetails)
                HTTP_GONE -> throw GoneException(errorDetails)
                HTTP_UNSUPPORTED_TYPE -> throw UnsupportedMediaTypeException(errorDetails)
                429 -> throw TooManyRequestsException(errorDetails)
                HTTP_INTERNAL_ERROR -> throw InternalServerErrorException(errorDetails)
                HTTP_NOT_IMPLEMENTED -> throw NotImplementedException(errorDetails)
                HTTP_BAD_GATEWAY -> throw BadGatewayException(errorDetails)
                HTTP_UNAVAILABLE -> throw ServiceUnavailableException(errorDetails)
                HTTP_GATEWAY_TIMEOUT -> throw GatewayTimeoutException(errorDetails)
                HTTP_VERSION -> throw HTTPVersionNotSupported(errorDetails)
                511 -> throw NetworkAuthenticationRequiredException(errorDetails)
                598 -> throw NetworkReadTimeoutException(errorDetails)
                599 -> throw NetworkConnectTimeoutException(errorDetails)
                else -> {
                    if (code in 100..199) throw CwaInformationalNotSupportedError(code, errorDetails)
                    if (code in 200..299) throw CwaSuccessResponseWithCodeMismatchNotSupportedError(
                        code,
                        errorDetails
                    )
                    if (code in 300..399) throw CwaRedirectNotSupportedError(code, errorDetails)
                    if (code in 400..499) throw CwaClientError(code, errorDetails)
                    if (code in 500..599) throw CwaServerError(code, errorDetails)
                    throw CwaWebException(code, errorDetails)
                }
            }
        } catch (err: SocketTimeoutException) {
            throw NetworkConnectTimeoutException(cause = err)
        } catch (err: UnknownHostException) {
            throw CwaUnknownHostException(cause = err)
        }
    }
}
