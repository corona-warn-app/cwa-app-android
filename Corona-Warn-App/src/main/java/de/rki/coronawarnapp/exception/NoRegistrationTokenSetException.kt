package de.rki.coronawarnapp.exception

class NoRegistrationTokenSetException :
    Exception("there is no valid registration token set in local storage")
