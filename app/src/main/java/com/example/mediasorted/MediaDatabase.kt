package com.example.mediasorted

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [MediaEventEntity::class],
    version = 2,
    exportSchema = false
)
abstract class MediaDatabase : RoomDatabase() {

    abstract fun mediaEventDao(): MediaEventDao

    companion object {

        @Volatile
        private var INSTANCE: MediaDatabase? = null

        // Migration from database version 1 to version 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {

                // Add the new eventKey column
                db.execSQL(
                    """
            ALTER TABLE media_events
            ADD COLUMN eventKey TEXT NOT NULL DEFAULT ''
            """
                )

                // Give every existing record a unique legacy key
                db.execSQL(
                    """
            UPDATE media_events
            SET eventKey = 'LEGACY_' || id
            WHERE eventKey = ''
            """
                )

                // Create the unique index
                db.execSQL(
                    """
            CREATE UNIQUE INDEX index_media_events_eventKey
            ON media_events(eventKey)
            """
                )
            }
        }

        fun getDatabase(context: Context): MediaDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MediaDatabase::class.java,
                    "mediasorted_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}