package com.example.dkalita.pipedriveapi.common

import android.support.v7.util.DiffUtil
import com.example.dkalita.pipedriveapi.datasource.DbPerson

class PersonsDiff(val oldItems: List<DbPerson>, val newItems: List<DbPerson>) : DiffUtil.Callback() {

	override fun getOldListSize() = oldItems.size

	override fun getNewListSize() = newItems.size

	override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
		return oldItems[oldItemPosition].id == newItems[newItemPosition].id
	}

	override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
		return oldItems[oldItemPosition] == newItems[newItemPosition]
	}
}
