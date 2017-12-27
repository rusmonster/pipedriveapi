package com.example.dkalita.pipedriveapi.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.databinding.ObservableField
import android.os.Handler
import com.example.dkalita.pipedriveapi.R
import com.example.dkalita.pipedriveapi.common.TokenManager
import com.example.dkalita.pipedriveapi.datasource.PipedriveApi
import com.example.dkalita.pipedriveapi.inject.ApplicationContext
import java.util.concurrent.Executor
import javax.inject.Inject

class LoginViewModel @Inject constructor(
		@ApplicationContext
		private val context: Context,
		private val executor: Executor,
		private val webService: PipedriveApi,
		private val tokenManager: TokenManager
) : ViewModel() {

	var login = ""

	var password = ""

	var token = ""

	var saveCredentials = true

	val isInProgress = ObservableField(false)

	val loginComplete = MutableLiveData<Boolean>()

	val error = MutableLiveData<String>()

	private val uiHandler = Handler()

	init {
		if (tokenManager.token.isNotEmpty()) {
			loginComplete.value = true
		}
	}

	fun attemptLogin() {
		if (isInProgress.get()) {
			return
		}

		if (loginUsingLoginAndPassword()) {
			return
		}

		if (loginUsingToken()) {
			return
		}

		error.value = context.getString(R.string.login_error_empty_fields)
	}

	private fun loginUsingLoginAndPassword(): Boolean {
		if (login.isEmpty() || password.isEmpty()) {
			return false
		}

		isInProgress.set(true)

		val localLogin = login
		val localPassword = password
		executor.execute {
			val authResult = webService.fetch { requestAuthorizations(localLogin, localPassword) }

			if (authResult.success && authResult.data!!.isNotEmpty()) {
				postValidToken(authResult.data.first().apiToken)
				return@execute
			}

			if (authResult.success) { // Empty data
				val message = context.getString(R.string.login_error_empty_auth)
				error.postValue(message)
			}
			else {
				val message = authResult.error.takeIf { !it.isNullOrBlank() }
						?: context.getString(R.string.login_error_unknown)
				error.postValue(message)
			}

			postInProgress(false)
		}

		return true
	}

	private fun loginUsingToken(): Boolean {
		if (token.isEmpty()) {
			return false
		}

		isInProgress.set(true)

		val localToken = token
		executor.execute {
			val isTokenValid = webService.checkToken(localToken)

			if (isTokenValid) {
				postValidToken(localToken)
			} else {
				val message = context.getString(R.string.login_error_invalid_token)
				error.postValue(message)
				postInProgress(false)
			}
		}

		return true
	}

	private fun postValidToken(validToken: String) {
		uiHandler.post {
			tokenManager.token = validToken

			if (saveCredentials) {
				tokenManager.saveToken()
			}

			loginComplete.value = true
		}
	}

	private fun postInProgress(inProgress: Boolean) {
		uiHandler.post {
			isInProgress.set(inProgress)
		}
	}
}
