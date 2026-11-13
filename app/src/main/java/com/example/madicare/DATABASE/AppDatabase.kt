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
        DoctorVisitEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicareDao(): MedicareDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
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
                      //  prepopulateDatabase(database.medicareDao())
                    }
                }
            }

//            suspend fun prepopulateDatabase(dao: MedicareDao) {
//                val defaultProfile = ProfileEntity(
//                    id = 1L,
//                    person_Frist_nameval = "John",
//                    person_Last_name = "Doe",
//                    phoneNumber = "+1234567890",
//                    emailaddress = "john.doe@example.com"
//                )
//                dao.insertProfile(defaultProfile)
//
//                dao.insertMedicine(
//                    medicine_details(
//                        userId = 1L,
//                        medicineName = "Paracetamol",
//                        strength = "500 mg",
//                        totalTabletsInPack = 30,
//                        remainingTablets = 20,
//                        dosePerTime = "1 Tablet",
//                        howOften = "Daily",
//                        time = "7:00 AM",
//                        bottleColorIndex = 0,
//                        isTaken = true
//                    )
//                )
//                dao.insertMedicine(
//                    medicine_details(
//                        userId = 1L,
//                        medicineName = "Vitamin D3",
//                        strength = "1000 IU",
//                        totalTabletsInPack = 30,
//                        remainingTablets = 15,
//                        dosePerTime = "1 Tablet",
//                        howOften = "Daily",
//                        time = "12:00 PM",
//                        bottleColorIndex = 1,
//                        isTaken = false
//                    )
//                )
//                dao.insertMedicine(
//                    medicine_details(
//                        userId = 1L,
//                        medicineName = "BP Medicine",
//                        strength = "5 mg",
//                        totalTabletsInPack = 30,
//                        remainingTablets = 8,
//                        dosePerTime = "1 Tablet",
//                        howOften = "Daily",
//                        time = "6:00 PM",
//                        bottleColorIndex = 2,
//                        isTaken = false
//                    )
//                )
//            }
        }
    }
}
