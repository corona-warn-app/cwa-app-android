package de.rki.coronawarnapp.exception

class NoGUIDOrTANSetException : Exception("there is no valid GUID or teleTAN set in local storage")
