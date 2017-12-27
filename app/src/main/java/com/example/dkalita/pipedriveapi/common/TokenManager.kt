package com.example.dkalita.pipedriveapi.common

import com.example.dkalita.pipedriveapi.datasource.PipedriveDatabase
import com.example.dkalita.pipedriveapi.datasource.clear
import com.example.dkalita.pipedriveapi.repository.SettingsRepository
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.properties.Delegates

@Singleton
class TokenManager @Inject constructor(
		private val executor: Executor,
		private val database: PipedriveDatabase,
		private val settingsRepository: SettingsRepository
) {

	var token by Delegates.observable(settingsRepository.apiToken) { _, _, _ -> onTokenChanged() }

	fun saveToken() {
		settingsRepository.apiToken = token
	}

	fun clearToken() {
		token = ""
		saveToken()
	}

	private fun onTokenChanged() {
		val bakHash = settingsRepository.tokenHash
		val newHash by lazy { token.sha256 }

		if (token.isNotEmpty() && newHash != bakHash) {
			settingsRepository.tokenHash = newHash
			clearDatabase();
		}
	}

	private fun clearDatabase() {
		executor.execute {
			database.clear()
		}
	}
}
