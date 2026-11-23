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

    suspend fun updateRemainingTablets(medicineId: Long, remaining: Int) {
        dao.updateRemainingTablets(medicineId, remaining)
    }

    suspend fun insertMedicine(medicine: medicine_details): Long {
        // Ensure default profile exists to satisfy foreign key constraint
        val existingProfile = dao.getProfile(currentUserId).firstOrNull()
        if (existingProfile == null) {
            dao.insertProfile(
                ProfileEntity(
                    id = currentUserId,
                    person_Frist_nameval = "User",
                    person_Last_name = "",
                    phoneNumber = "",
                    emailaddress = ""
                )
            )
        }
        return dao.insertMedicine(medicine)
    }

    // WHAT: Fixed the typo from 'delateMedicine' to 'deleteMedicine'
    suspend fun deleteMedicine(medicine: medicine_details) {
        dao.deleteMedicine(medicine = medicine)
    }
}
