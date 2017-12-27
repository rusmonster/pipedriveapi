package com.example.dkalita.pipedriveapi.activity

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Color.WHITE
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import com.example.dkalita.pipedriveapi.R
import com.example.dkalita.pipedriveapi.common.lazyViewModel
import com.example.dkalita.pipedriveapi.common.observeNotNull
import com.example.dkalita.pipedriveapi.common.observeTrue
import com.example.dkalita.pipedriveapi.databinding.ActivityLoginBinding
import com.example.dkalita.pipedriveapi.inject.createInjector
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

	companion object {

		fun start(context: Context) {
			val intent = Intent(context, LoginActivity::class.java)
			context.startActivity(intent)
		}
	}

	private val viewModel by lazyViewModel { createInjector().getLoginViewModel() }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		window.setBackgroundDrawable(ColorDrawable(WHITE))

		val binding = DataBindingUtil.setContentView<ActivityLoginBinding>(
				this, R.layout.activity_login)
		binding.model = viewModel

		val onEditorActionListener = OnEditorActionListener { _, id, _ ->
			if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
				viewModel.attemptLogin()
				return@OnEditorActionListener true
			}
			return@OnEditorActionListener false
		}

		loginPasswordView.setOnEditorActionListener(onEditorActionListener)
		loginTokenView.setOnEditorActionListener(onEditorActionListener)

		viewModel.error.observeNotNull(this) { message ->
			showError(message)
			viewModel.error.value = null
		}

		viewModel.loginComplete.observeTrue(this) {
			MainActivity.start(this)
			finish()
		}
	}

	private fun showError(message: String) {
		Snackbar
				.make(loginFormView, message, Snackbar.LENGTH_LONG)
				.show();
	}
}
