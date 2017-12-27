package com.example.dkalita.pipedriveapi.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.example.dkalita.pipedriveapi.datasource.DbContact
import com.example.dkalita.pipedriveapi.datasource.DbContact.Type
import com.example.dkalita.pipedriveapi.datasource.DbContact.Type.EMAIL
import com.example.dkalita.pipedriveapi.datasource.DbContact.Type.PHONE
import com.example.dkalita.pipedriveapi.datasource.DbPerson
import com.example.dkalita.pipedriveapi.datasource.PipedriveApi
import com.example.dkalita.pipedriveapi.datasource.PipedriveApi.ApiContact
import com.example.dkalita.pipedriveapi.datasource.PipedriveApi.ApiPerson
import com.example.dkalita.pipedriveapi.datasource.PipedriveDatabase
import java.util.concurrent.Executor
import javax.inject.Inject

class PersonRepository @Inject constructor(
		private val executor: Executor,
		private val database: PipedriveDatabase,
		private val webService: PipedriveApi
) {
	companion object {

		private val PAGE_SIZE = 30
	}

	private val personDao = database.personDao()

	private val contactDao = database.contactDao()

	fun loadPersons(): LiveData<List<DbPerson>> {
		fetchFirstPageIfDbEmpty()
		return personDao.load()
	}

	fun loadPerson(personId: Int): LiveData<DbPerson> {
		fetchPerson(personId)
		return personDao.load(personId)
	}

	fun loadContacts(personId: Int) = contactDao.load(personId)

	fun refreshPersons(): LiveData<RefreshAnswer> {
		var answer = RefreshAnswer(isRefreshing = true)
		val result = MutableLiveData<RefreshAnswer>().apply { value = answer }

		executor.execute {
			val apiResult = webService.fetch { requestPersons(limit = PAGE_SIZE) }

			if (!apiResult.success) {
				answer = answer.copy(refreshError = true, errorMessage = apiResult.error ?: "")
			} else {
				personDao.clear()
				apiResult.data?.let { insertIntoDatabase(it) }
			}

			answer = answer.copy(isRefreshing = false)
			result.postValue(answer)
		}

		return result
	}

	fun fetchNextPage() {
		executor.execute {
			val startIndex = personDao.getCount()
			fetchPage(startIndex)
		}
	}

	private fun fetchFirstPageIfDbEmpty() {
		executor.execute {
			val count = personDao.getCount()
			if (count == 0) fetchPage(0)
		}
	}

	private fun fetchPage(startIndex: Int) {
		val apiResult = webService.fetch { requestPersons(startIndex, limit = PAGE_SIZE) }
		apiResult.data?.let { insertIntoDatabase(it) }
	}

	private fun insertIntoDatabase(persons: List<ApiPerson>) {
		database.runInTransaction {
			personDao.insert(persons.map { it.toDbPerson() })

			persons.forEach { person ->
				val phones = person.phone.map { it.toDbContact(person.id, PHONE) }
				val mails = person.email.map { it.toDbContact(person.id, EMAIL) }
				contactDao.insert(phones + mails)
			}
		}
	}

	private fun fetchPerson(personId: Int) {
		executor.execute {
			val result = webService.fetch { requestPerson(personId) }
			val person = result.data ?: return@execute
			insertIntoDatabase(listOf(person))
		}
	}

	private fun ApiPerson.toDbPerson() = DbPerson(
			id = id,
			name = name ?: "",
			photoUrl = pictureId?.pictures?.url ?: ""
	)

	private fun ApiContact.toDbContact(personId: Int, type: Type) = DbContact(
			personId = personId,
			type = type,
			label = label ?: "",
			value = value ?: "",
			isPrimary = primary ?: false
	)

	data class RefreshAnswer(
			val isRefreshing: Boolean = false,
			val refreshError: Boolean = false,
			val errorMessage: String = ""
	)
}
