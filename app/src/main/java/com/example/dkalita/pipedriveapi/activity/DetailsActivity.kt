package com.example.dkalita.pipedriveapi.activity

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.dkalita.pipedriveapi.R
import com.example.dkalita.pipedriveapi.common.GlideApp
import com.example.dkalita.pipedriveapi.common.observeNotNull
import com.example.dkalita.pipedriveapi.databinding.ActivityDetailsBinding
import com.example.dkalita.pipedriveapi.databinding.ContactItemBinding
import com.example.dkalita.pipedriveapi.inject.createInjector
import com.example.dkalita.pipedriveapi.repository.PersonRepository
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.android.synthetic.main.details_content.*
import javax.inject.Inject

class DetailsActivity : AppCompatActivity() {

	companion object {

		private val KEY_PERSON_ID = "KEY_PERSON_ID"

		fun start(context: Context, personId: Int) {
			val intent = Intent(context, DetailsActivity::class.java).apply {
				putExtra(KEY_PERSON_ID, personId)
			}
			context.startActivity(intent)
		}
	}

	@Inject
	lateinit var personRepository: PersonRepository

	private val personId get() = intent.getIntExtra(KEY_PERSON_ID, -1)

	private val person by lazy { personRepository.loadPerson(personId) }

	private val contacts by lazy { personRepository.loadContacts(personId) }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		createInjector().inject(this)

		val binding = DataBindingUtil.setContentView<ActivityDetailsBinding>(
				this, R.layout.activity_details)

		setSupportActionBar(toolbarView)

		person.observeNotNull(this) { person ->
			binding.person = person

			GlideApp.with(this)
					.load(person.photoUrl)
					.diskCacheStrategy(DiskCacheStrategy.RESOURCE)
					.placeholder(R.drawable.ic_face)
					.into(imageView)
		}

		contacts.observeNotNull(this) { updateContacts() }
	}

	private fun updateContacts() {
		contactsContainerView.removeAllViews()
		contacts.value?.forEach { contact ->
			val binding = ContactItemBinding.inflate(layoutInflater, contactsContainerView, true)
			binding.contact = contact
		}
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		if (item.itemId == android.R.id.home) {
			onBackPressed()
			return true
		}
		return super.onOptionsItemSelected(item)
	}
}
