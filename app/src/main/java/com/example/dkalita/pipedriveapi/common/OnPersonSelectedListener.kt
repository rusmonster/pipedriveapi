package com.example.dkalita.pipedriveapi.common

import com.example.dkalita.pipedriveapi.datasource.DbPerson

interface OnPersonSelectedListener {

	fun onPersonSelected(person: DbPerson)
}
