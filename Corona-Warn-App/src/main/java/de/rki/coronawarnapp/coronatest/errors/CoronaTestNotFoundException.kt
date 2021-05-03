package de.rki.coronawarnapp.coronatest.errors

class ModifyNotFoundTestException(message: String) : IllegalArgumentException(message)

class RemoveTestNotFoundException(message: String) : IllegalArgumentException(message)
