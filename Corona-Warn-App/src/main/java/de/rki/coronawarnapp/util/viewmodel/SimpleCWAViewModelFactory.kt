package de.rki.coronawarnapp.util.viewmodel

import de.rki.coronawarnapp.storage.SubmissionRepository

interface SimpleCWAViewModelFactory<T : CWAViewModel> : CWAViewModelFactory<T> {
    fun create(): T
}

interface InjectedSubmissionViewModelFactory<T : CWAViewModel> : CWAViewModelFactory<T> {
    fun create(submissionRepository: SubmissionRepository): T
}
