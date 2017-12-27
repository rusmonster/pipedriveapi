package com.example.dkalita.pipedriveapi.common

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider.Factory
import android.arch.lifecycle.ViewModelProviders
import android.support.v4.app.FragmentActivity
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import javax.net.ssl.HttpsURLConnection


inline fun <T> LiveData<T>.observeNotNull(owner: LifecycleOwner, crossinline block: (T) -> Unit) {
	val observer = Observer<T> { if (it != null) block(it) }
	observe(owner, observer)
}

inline fun LiveData<Boolean>.observeTrue(owner: LifecycleOwner, crossinline block: () -> Unit) {
	val observer = Observer<Boolean> { if (it == true) block() }
	observe(owner, observer)
}

inline fun <reified T : ViewModel> FragmentActivity.lazyViewModel(
		crossinline create: () -> T) = lazy(LazyThreadSafetyMode.NONE) {

	val factory = object : Factory {
		@Suppress("UNCHECKED_CAST")
		override fun <R : ViewModel> create(modelClass: Class<R>): R = create() as R
	}
	return@lazy ViewModelProviders.of(this, factory).get(T::class.java)
}

fun URL.post(vararg params: Pair<String, String>): String {
	val conn = openConnection() as HttpsURLConnection
	conn.doInput = true
	conn.doOutput = true

	val encodedParams = params.joinToString(separator = "&") { (key, value) ->
			val encodedKey = URLEncoder.encode(key, "UTF-8")
			val encodedValue = URLEncoder.encode(value, "UTF-8")
			return@joinToString "$encodedKey=$encodedValue"
	}

	conn.outputStream.bufferedWriter().use { it.write(encodedParams) }
	return conn.inputStream.bufferedReader().use { it.readText() }
}

val String.sha256: String
	get() {
		val bytes = toByteArray()
		val md = MessageDigest.getInstance("SHA-256")
		val digest = md.digest(bytes)
		return digest.fold("") { str, byte -> str + "%02x".format(byte) }
	}
