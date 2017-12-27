package com.example.dkalita.pipedriveapi.inject

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.RUNTIME

@Qualifier
@Retention(RUNTIME)
annotation class ApplicationContext

@Qualifier
@Retention(RUNTIME)
annotation class ApiToken
