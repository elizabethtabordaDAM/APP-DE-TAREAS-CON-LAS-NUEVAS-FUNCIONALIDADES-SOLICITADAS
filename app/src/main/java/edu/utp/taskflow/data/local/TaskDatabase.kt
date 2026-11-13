package edu.utp.taskflow.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


@Database(
    entities = [TaskEntity::class, UserEntity::class],
    version = 4,
    exportSchema = false
)
abstract class TaskDatabase: RoomDatabase() {

    abstract fun taskDao(): TaskDao

    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        // Migración 2 -> 3: cada tarea ahora tiene un dueño (userId)
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE tasks ADD COLUMN userId INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        // Migración 3 -> 4: cada usuario ahora tiene un correo
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE users ADD COLUMN email TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        fun getDatabase(
            context: Context
        ): TaskDatabase {

            return INSTANCE ?: synchronized(this) {

                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "taskflow_database"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also {
                        INSTANCE = it
                    }
            }
        }
    }
}