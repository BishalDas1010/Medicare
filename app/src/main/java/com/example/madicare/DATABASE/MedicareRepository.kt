package com.example.madicare.DATABASE

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicareRepository @Inject constructor(
    private val dao: MedicareDao
) {
    private val currentUserId = 1L // Hardcoded for now

    val userProfile: Flow<ProfileEntity?> = dao.getProfile(currentUserId)
    val medicines: Flow<List<medicine_details>> = dao.getMedicines(currentUserId)

    // WHY: We pass the ID to efficiently update just the 'isTaken' column in the database.
    suspend fun markMedicineAsTaken(medicineId: Long) {
        dao.updateMedicineTakenStatus(medicineId = medicineId, isTaken = true)
    }

    suspend fun insertMedicine(medicine: medicine_details): Long {
        return dao.insertMedicine(medicine)
    }

    // WHAT: Fixed the typo from 'delateMedicine' to 'deleteMedicine'
    suspend fun deleteMedicine(medicine: medicine_details) {
        dao.deleteMedicine(medicine = medicine)
    }

    // WHY: This function now correctly checks if the profile exists before inserting data.
    // If we don't check, every time the app opens, it will overwrite the user's progress.
//    suspend fun seedDatabaseIfEmpty() {
//        // Check if the profile already exists in the database
//        val existingProfile = dao.getProfile(currentUserId).firstOrNull()
//
//        // Only insert dummy data if the database is truly empty
//        if (existingProfile == null) {
//            val profileId = dao.insertProfile(
//                ProfileEntity(
//                    id = currentUserId,
//                    person_Frist_nameval = "Bishal",
//                    person_Last_name = "Das",
//                    phoneNumber = "9366970932",
//                    emailaddress = "vihsaldasyw@Gmail.com"
//                )
//            )
//
//            // WHAT: Insert 3 dummy medicines to match the UI design (Taken, Next, Upcoming)
//            dao.insertMedicine(
//                medicine_details(
//                    userId = profileId,
//                    medicineName = "My Medication 01",
//                    strength = "500mg",
//                    totalTabletsInPack = 30,
//                    dosePerTime = "1 Tablet",
//                    howOften = "Daily",
//                    time = "7:00 AM",
//                    isTaken = true // This will show as TAKEN (Green)
//                )
//            )
//
//            dao.insertMedicine(
//                medicine_details(
//                    userId = profileId,
//                    medicineName = "My Medication 01",
//                    strength = "500mg",
//                    totalTabletsInPack = 30,
//                    dosePerTime = "1 Tablet",
//                    howOften = "Daily",
//                    time = "12:00 PM",
//                    isTaken = false // This will show as NEXT (Blue)
//                )
//            )
//

}
