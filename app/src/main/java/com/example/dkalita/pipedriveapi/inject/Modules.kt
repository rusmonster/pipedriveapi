package com.example.dkalita.pipedriveapi.inject

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context
import com.example.dkalita.pipedriveapi.common.TokenManager
import com.example.dkalita.pipedriveapi.datasource.PipedriveDatabase
import dagger.Module
import dagger.Provides
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
class ApplicationModule(val application: Application) {

	@Provides
	@Singleton
	@ApplicationContext
	fun provideContext(): Context = application

	@Provides
	@Singleton
	fun provideExecutor(): Executor = Executors.newSingleThreadExecutor()

	@Provides
	@Singleton
	fun provideDatabase(@ApplicationContext context: Context): PipedriveDatabase {
		return Room.databaseBuilder(context, PipedriveDatabase::class.java, "pipedrive-db").build()
	}

	@Provides
	@ApiToken
	fun provideApiToken(tokenManager: TokenManager) = tokenManager.token
}
