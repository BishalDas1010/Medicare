package com.example.madicare.DATABASE

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProfileEntity::class,
        medicine_details::class,
        HealthInfoEntity::class,
        DoctorVisitEntity::class,
        TestResultEntity::class,
        ReminderEventLogEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicareDao(): MedicareDao
/*
In Kotlin, a companion object lets you access
 properties and functions using the class name,
 without creating an object of that class.

 */
    companion object {

        /*
        * @Volatile helps make sure that
        * when one thread updates INSTANCE, other threads can see the latest value.*/
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            /*The function checks whether a database already exists.*/
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medicare_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        // DB created
                    }
                }
            }
        }
    }
}
