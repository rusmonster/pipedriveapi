package com.example.dkalita.pipedriveapi.datasource

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Database
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.ForeignKey.CASCADE
import android.arch.persistence.room.Index
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.Query
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverter
import android.arch.persistence.room.TypeConverters

@Entity(tableName = "person")
data class DbPerson(
		@PrimaryKey
		val id: Int,
		val name: String,
		val photoUrl: String
)

@Entity(
		tableName = "contact",
		foreignKeys = [
			ForeignKey(
					entity = DbPerson::class,
					parentColumns = ["id"],
					childColumns = ["personId"],
					onDelete = CASCADE
			)
		],
		indices = [Index("personId")]
)
data class DbContact(
		@PrimaryKey(autoGenerate = true)
		val id: Int = 0,
		val personId: Int,
		val type: Type,
		val label: String,
		val value: String,
		val isPrimary: Boolean

) {
	enum class Type(private val value: Int) {
		EMAIL(0),
		PHONE(1);

		companion object {

			private val map = values().associateBy { it.value }
		}

		class Converters {
			@TypeConverter
			fun toInt(type: Type) = type.value

			@TypeConverter
			fun fromInt(value: Int) = map[value]!!
		}
	}
}

@Dao
interface PersonDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(persons: List<DbPerson>)

	@Query("DELETE FROM person")
	fun clear()

	@Query("SELECT * FROM person ORDER BY name")
	fun load(): LiveData<List<DbPerson>>

	@Query("SELECT * FROM person WHERE id = :personId")
	fun load(personId: Int): LiveData<DbPerson>

	@Query("SELECT COUNT(id) FROM person")
	fun getCount(): Int
}

@Dao
interface ContactDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(contacts: List<DbContact>)

	@Query("DELETE FROM contact")
	fun clear()

	@Query("SELECT * FROM contact WHERE personId = :personId")
	fun load(personId: Int): LiveData<List<DbContact>>
}

@Database(entities = [DbPerson::class, DbContact::class], version = 1, exportSchema = false)
@TypeConverters(DbContact.Type.Converters::class)
abstract class PipedriveDatabase : RoomDatabase() {

	abstract fun personDao(): PersonDao

	abstract fun contactDao(): ContactDao
}

fun PipedriveDatabase.clear() = runInTransaction {
	contactDao().clear()
	personDao().clear()
}
