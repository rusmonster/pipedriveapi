package com.example.dkalita.pipedriveapi.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import com.example.dkalita.pipedriveapi.R
import com.example.dkalita.pipedriveapi.common.OnPersonSelectedListener
import com.example.dkalita.pipedriveapi.common.PersonsDiff
import com.example.dkalita.pipedriveapi.common.TokenManager
import com.example.dkalita.pipedriveapi.common.observeNotNull
import com.example.dkalita.pipedriveapi.databinding.MainItemBinding
import com.example.dkalita.pipedriveapi.datasource.DbPerson
import com.example.dkalita.pipedriveapi.inject.createInjector
import com.example.dkalita.pipedriveapi.repository.PersonRepository
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity(), OnPersonSelectedListener {

	companion object {

		fun start(context: Context) {
			val intent = Intent(context, MainActivity::class.java)
			context.startActivity(intent)
		}
	}

	@Inject
	lateinit var tokenManager: TokenManager

	@Inject
	lateinit var personRepository: PersonRepository

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		createInjector().inject(this)

		setContentView(R.layout.activity_main)

		swipeToRefreshView.setColorSchemeResources(R.color.colorAccent)
		swipeToRefreshView.setOnRefreshListener { refreshPersons() }

		val adapter = Adapter()
		mainRecyclerView.adapter = adapter
		mainRecyclerView.layoutManager = LinearLayoutManager(this)

		personRepository.loadPersons().observeNotNull(this) { adapter.items = it }
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		menuInflater.inflate(R.menu.main_menu, menu)
		return super.onCreateOptionsMenu(menu)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			R.id.menu_logout -> {
				tokenManager.clearToken()
				LoginActivity.start(this)
				finish()
				return true
			}
		}

		return super.onOptionsItemSelected(item)
	}

	override fun onPersonSelected(person: DbPerson) = DetailsActivity.start(this, person.id)

	private fun refreshPersons() {
		personRepository.refreshPersons().observeNotNull(this) { answer ->
			if (!answer.isRefreshing) {
				swipeToRefreshView.isRefreshing = false
				mainRecyclerView.adapter.notifyDataSetChanged()
			}

			if (answer.refreshError) {
				val message = answer.errorMessage.takeIf { it.isNotEmpty() }
						?: getString(R.string.error_fetch_default)

				showError(message)
			}
		}
	}

	private fun showError(message: String) {
		Snackbar
				.make(mainRecyclerView, message, Snackbar.LENGTH_LONG)
				.setAction(R.string.action_retry) { refreshPersons() }
				.show();
	}

	private inner class Adapter : RecyclerView.Adapter<ViewHolder>() {

		var items: List<DbPerson> by Delegates.observable(emptyList()) { _, old, new ->
			DiffUtil.calculateDiff(PersonsDiff(old, new)).dispatchUpdatesTo(this)
		}

		override fun getItemCount() = items.size

		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
			val binding = MainItemBinding.inflate(layoutInflater, parent, false)
			binding.listener = this@MainActivity
			return ViewHolder(binding)
		}

		override fun onBindViewHolder(holder: ViewHolder, position: Int) {
			val person = items[position]
			holder.binding.person = person

			if (position == items.size - 1) {
				personRepository.fetchNextPage()
			}
		}
	}

	private class ViewHolder(val binding: MainItemBinding) : RecyclerView.ViewHolder(binding.root)
}
