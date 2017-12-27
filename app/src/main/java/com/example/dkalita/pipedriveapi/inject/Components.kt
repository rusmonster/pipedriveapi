package com.example.dkalita.pipedriveapi.inject

import com.example.dkalita.pipedriveapi.activity.DetailsActivity
import com.example.dkalita.pipedriveapi.activity.MainActivity
import com.example.dkalita.pipedriveapi.viewmodel.LoginViewModel
import dagger.Component
import dagger.Subcomponent
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {

	fun loginActivityComponent(): LoginActivityComponent

	fun mainActivityComponent(): MainActivityComponent

	fun detailsActivityComponent(): DetailsActivityComponent
}

@Subcomponent
interface LoginActivityComponent {

	fun getLoginViewModel(): LoginViewModel
}

@Subcomponent
interface MainActivityComponent {

	fun inject(activity: MainActivity)
}

@Subcomponent
interface DetailsActivityComponent {

	fun inject(activity: DetailsActivity)
}
