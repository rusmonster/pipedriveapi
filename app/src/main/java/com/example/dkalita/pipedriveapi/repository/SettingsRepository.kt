package com.example.dkalita.pipedriveapi.repository

import android.content.Context
import android.preference.PreferenceManager
import com.example.dkalita.pipedriveapi.inject.ApplicationContext
import javax.inject.Inject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SettingsRepository @Inject constructor(
		@ApplicationContext
		context: Context
) {

	var apiToken by stringPreference()

	var tokenHash by stringPreference()

	private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

	fun stringPreference() = object : ReadWriteProperty<Any?, String> {

		override fun getValue(thisRef: Any?, property: KProperty<*>): String {
			return sharedPreferences.getString(property.name, "")
		}

		override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
			sharedPreferences.edit()
					.putString(property.name, value)
					.apply()
		}
	}
}
