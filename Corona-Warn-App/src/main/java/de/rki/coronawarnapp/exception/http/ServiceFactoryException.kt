package de.rki.coronawarnapp.exception.http

class ServiceFactoryException(cause: Throwable) :
    Exception("error inside the service factory", cause)
