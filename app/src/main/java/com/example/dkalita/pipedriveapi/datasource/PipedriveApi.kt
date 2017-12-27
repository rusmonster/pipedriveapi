package com.example.dkalita.pipedriveapi.datasource

import com.example.dkalita.pipedriveapi.common.post
import com.example.dkalita.pipedriveapi.inject.ApiToken
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.net.URL
import javax.inject.Inject


class PipedriveApi @Inject constructor(
		@ApiToken
		private val apiToken: String
) {
	private val ENDPOINT = "https://dkalitainc.pipedrive.com/v1"

	fun checkToken(token: String): Boolean {
		val url = "$ENDPOINT/persons".addToken(token)
		val result: ApiResult<List<ApiPerson>> = fetch { get(url) }
		return result.success
	}

	inline fun <reified T : ApiResult<*>> fetch(request: PipedriveApi.() -> T): T {
		try {
			return request()
		} catch (e: Exception) {
			return T::class.java.newInstance()
		}
	}

	fun requestAuthorizations(login: String, password: String): ApiResult<List<ApiAuth>> {
		return post(
				"$ENDPOINT/authorizations",
				"email" to login,
				"password" to password)
	}

	fun requestPersons(start: Int = 0, limit: Int): ApiResult<List<ApiPerson>> {
		val url = "$ENDPOINT/persons".addToken() + "&start=$start&limit=$limit&sort=name"
		return get(url)
	}

	fun requestPerson(personId: Int): ApiResult<ApiPerson> = get("$ENDPOINT/persons/$personId".addToken())

	private inline fun <reified T> get(url: String): T {
		val response = URL(url).readText()
		return parse(response)
	}

	private inline fun <reified T> post(url: String, vararg params: Pair<String, String>): T {
		val response = URL(url).post(*params)
		return parse(response)
	}

	private inline fun <reified T> parse(apiResponse: String): T {
		val type = object : TypeToken<T>() {}.type
		return Gson().fromJson(apiResponse, type)
	}

	private fun String.addToken(token: String = apiToken) = "$this?api_token=$token"

	data class ApiResult<T>(
			val success: Boolean = false,
			val error: String? = null,
			val data: T? = null
	)

	data class ApiAuth(
			@SerializedName("api_token")
			val apiToken: String
	)

	data class ApiPerson(
			val id: Int,
			val name: String?,
			val phone: List<ApiContact>,
			val email: List<ApiContact>,
			@SerializedName("picture_id")
			val pictureId: ApiPictureId?
	)

	data class ApiContact(
			val label: String?,
			val value: String?,
			val primary: Boolean?
	)

	data class ApiPictureId(
		val pictures: ApiPicture?
	)

	data class ApiPicture(
			@SerializedName("512")
			val url: String?
	)
}
