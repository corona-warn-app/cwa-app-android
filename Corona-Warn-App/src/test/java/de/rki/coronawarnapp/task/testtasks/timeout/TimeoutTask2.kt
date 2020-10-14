package de.rki.coronawarnapp.task.testtasks.timeout

import javax.inject.Provider

class TimeoutTask2 : BaseTimeoutTask() {

    class Factory constructor(taskByDagger: Provider<BaseTimeoutTask>) :
        BaseTimeoutTask.Factory(taskByDagger)
}
