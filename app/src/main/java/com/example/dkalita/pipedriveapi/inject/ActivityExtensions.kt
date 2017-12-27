package com.example.dkalita.pipedriveapi.inject

import android.app.Activity
import com.example.dkalita.pipedriveapi.activity.DetailsActivity
import com.example.dkalita.pipedriveapi.activity.MainActivity
import com.example.dkalita.pipedriveapi.PipedriveApplication
import com.example.dkalita.pipedriveapi.activity.LoginActivity

private val Activity.applicationComponent get() = (application as PipedriveApplication).component

fun LoginActivity.createInjector() = applicationComponent.loginActivityComponent()

fun MainActivity.createInjector() = applicationComponent.mainActivityComponent()

fun DetailsActivity.createInjector() = applicationComponent.detailsActivityComponent()
