package com.example.dkalita.pipedriveapi

import android.arch.core.executor.ArchTaskExecutor
import android.content.Context
import android.os.Handler
import com.example.dkalita.pipedriveapi.common.TokenManager
import com.example.dkalita.pipedriveapi.datasource.PipedriveApi
import com.example.dkalita.pipedriveapi.datasource.PipedriveApi.ApiAuth
import com.example.dkalita.pipedriveapi.datasource.PipedriveApi.ApiResult
import com.example.dkalita.pipedriveapi.viewmodel.LoginViewModel
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers.anyInt
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import java.util.concurrent.Executor
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(PowerMockRunner::class)
@PrepareForTest(
		PipedriveApi::class,
		TokenManager::class,
		LoginViewModel::class
)
class LoginViewModelTest {

	var loginViewModel by Delegates.notNull<LoginViewModel>()

	@Mock
	lateinit var context: Context

	@Mock
	lateinit var executor: Executor

	@Mock
	lateinit var wevService: PipedriveApi

	@Mock
	lateinit var tokenManager: TokenManager

	@Mock
	lateinit var handler: Handler

	@Before
	fun setUp() {
		ArchTaskExecutor.getInstance().setDelegate(ImmediateExecutor())

		whenCall(context.getString(anyInt())).then { it.arguments[0].toString() }
		whenCall(executor.execute(any())).then { (it.arguments[0] as Runnable).run() }
		whenCall(tokenManager.token).thenReturn("")

		whenNew(Handler::class)
				.withNoArguments()
				.thenReturn(handler)

		whenCall(handler.post(any())).then { (it.arguments[0] as Runnable).run(); true }

		loginViewModel = LoginViewModel(context, executor, wevService, tokenManager)
	}

	@After
	fun tearDown() {
		verify(tokenManager, atLeastOnce()).token
		verifyNoMoreInteractions(tokenManager)
	}

	@Test
	fun noUsername() {
		loginViewModel.login = ""

		loginViewModel.attemptLogin()

		assertNotEquals(true, loginViewModel.loginComplete.value)
		assertEquals("${R.string.login_error_empty_fields}", loginViewModel.error.value)
	}

	@Test
	fun noPassword() {
		loginViewModel.login = "testUser"
		loginViewModel.password = ""

		loginViewModel.attemptLogin()

		assertNotEquals(true, loginViewModel.loginComplete.value)
		assertEquals("${R.string.login_error_empty_fields}", loginViewModel.error.value)
	}

	@Test
	fun loginWithUsernameSuccessful() {
		loginViewModel.login = "testUser"
		loginViewModel.password = "testPassword"

		val result = ApiResult(
				success = true,
				data = listOf(ApiAuth("testToken")))
		whenCall(wevService.requestAuthorizations("testUser", "testPassword")).thenReturn(result)

		loginViewModel.attemptLogin()

		assertTrue(loginViewModel.loginComplete.value!!)
		assertNull(loginViewModel.error.value)

		verify(tokenManager).token = "testToken"
		verify(tokenManager).saveToken()
	}

	@Test
	fun loginWithUsernameFailed() {
		loginViewModel.login = "testUser"
		loginViewModel.password = "testPassword"

		whenCall(wevService.requestAuthorizations("testUser", "testPassword"))
				.thenReturn(ApiResult(success = false))

		loginViewModel.attemptLogin()

		assertNotEquals(true, loginViewModel.loginComplete.value)
		assertEquals("${R.string.login_error_unknown}", loginViewModel.error.value)
	}

	@Test
	fun loginWithUsernameFailedWithError() {
		loginViewModel.login = "testUser"
		loginViewModel.password = "testPassword"

		whenCall(wevService.requestAuthorizations("testUser", "testPassword"))
				.thenReturn(ApiResult(success = false, error = "testError"))

		loginViewModel.attemptLogin()

		assertNotEquals(true, loginViewModel.loginComplete.value)
		assertEquals("testError", loginViewModel.error.value)
	}

	@Test
	fun loginWithTokenSuccessful() {
		loginViewModel.login = ""
		loginViewModel.token = "testToken"
		loginViewModel.saveCredentials = false

		whenCall(wevService.checkToken("testToken")).thenReturn(true)

		loginViewModel.attemptLogin()

		assertTrue(loginViewModel.loginComplete.value!!)
		assertNull(loginViewModel.error.value)

		verify(tokenManager).token = "testToken"
	}

	@Test
	fun loginWithTokenFailed() {
		loginViewModel.login = ""
		loginViewModel.token = "testToken"

		whenCall(wevService.checkToken("testToken")).thenReturn(false)

		loginViewModel.attemptLogin()

		assertNotEquals(true, loginViewModel.loginComplete.value)
		assertEquals("${R.string.login_error_invalid_token}", loginViewModel.error.value)
	}

	@Test
	fun autoLogin() {
		whenCall(tokenManager.token).thenReturn("testToken")

		loginViewModel = LoginViewModel(context, executor, wevService, tokenManager)

		assertTrue(loginViewModel.loginComplete.value!!)
		assertNull(loginViewModel.error.value)
	}
}
