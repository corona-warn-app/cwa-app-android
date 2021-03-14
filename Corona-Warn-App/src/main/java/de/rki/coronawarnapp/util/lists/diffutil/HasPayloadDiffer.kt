package de.rki.coronawarnapp.util.lists.diffutil

interface HasPayloadDiffer {
    fun diffPayload(old: Any, new: Any): Any?
}
