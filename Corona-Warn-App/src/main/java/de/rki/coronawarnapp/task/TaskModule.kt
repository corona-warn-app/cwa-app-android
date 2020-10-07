package de.rki.coronawarnapp.task

import dagger.Module
import de.rki.coronawarnapp.task.example.ExampleTaskModule

@Module(
    includes = [
        ExampleTaskModule::class]
)
class TaskModule 
