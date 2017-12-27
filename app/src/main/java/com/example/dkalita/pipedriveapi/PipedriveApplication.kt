package com.example.dkalita.pipedriveapi

import android.app.Application
import com.example.dkalita.pipedriveapi.inject.ApplicationModule
import com.example.dkalita.pipedriveapi.inject.DaggerApplicationComponent

class PipedriveApplication : Application() {

	val component by lazy {
		DaggerApplicationComponent.builder()
				.applicationModule(ApplicationModule(this))
				.build()
	}
}
