package com.example.cuentaregresivaeventos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [EventEntity::class],
    version = 2,
    exportSchema = false
)
abstract class EventDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao

    companion object {
        @Volatile
        private var INSTANCE: EventDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // RENAME COLUMN requiere SQLite 3.25 (API 30+); recrear la tabla funciona en minSdk 24
                database.execSQL(
                    "CREATE TABLE events_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "title TEXT NOT NULL, place TEXT NOT NULL, city TEXT NOT NULL, " +
                        "description TEXT, imagePath TEXT, dateTimeMillis INTEGER NOT NULL)"
                )
                database.execSQL(
                    "INSERT INTO events_new (id, title, place, city, description, imagePath, dateTimeMillis) " +
                        "SELECT id, title, place, city, description, imageUri, dateTimeMillis FROM events"
                )
                database.execSQL("DROP TABLE events")
                database.execSQL("ALTER TABLE events_new RENAME TO events")
            }
        }

        fun getInstance(context: Context): EventDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    EventDatabase::class.java,
                    "events_db"
                ).addMigrations(MIGRATION_1_2).build().also { INSTANCE = it }
            }
        }
    }
}