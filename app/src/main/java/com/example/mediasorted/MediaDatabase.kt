package com.example.mediasorted

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        MediaEventEntity::class,
        ChatEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class MediaDatabase : RoomDatabase() {

    abstract fun mediaEventDao(): MediaEventDao

    abstract fun chatDao(): ChatDao

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

        // Migration from database version 2 to version 3
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {

                // Create the chats table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS chats (
                        chatId TEXT NOT NULL,
                        displayName TEXT NOT NULL,
                        previousDisplayName TEXT,
                        updatedAt INTEGER NOT NULL,
                        PRIMARY KEY(chatId)
                    )
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
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3
                    )
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}