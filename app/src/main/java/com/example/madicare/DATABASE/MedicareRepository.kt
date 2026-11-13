package com.example.madicare.DATABASE

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class MedicareRepository(private val dao: MedicareDao) {
    fun getMedicines(userId: Long = 1L): Flow<List<medicine_details>> = dao.getMedicines(userId)

    fun getProfile(userId: Long = 1L): Flow<ProfileEntity?> = dao.getProfile(userId)

    suspend fun ensureDefaultProfile(userId: Long = 1L) {
        val existingProfile = dao.getProfile(userId).firstOrNull()
        if (existingProfile == null) {
            val defaultProfile = ProfileEntity(
                id = userId,
                person_Frist_nameval = "John",
                person_Last_name = "Doe",
                phoneNumber = "+1234567890",
                emailaddress = "john.doe@example.com"
            )
            dao.insertProfile(defaultProfile)
        }
    }

    suspend fun insertMedicine(medicine: medicine_details): Long {
        ensureDefaultProfile(medicine.userId)
        return dao.insertMedicine(medicine)
    }

    suspend fun updateMedicineTakenStatus(medicineId: Long, isTaken: Boolean) {
        dao.updateMedicineTakenStatus(medicineId, isTaken)
    }

    suspend fun deleteMedicine(medicine: medicine_details) {
        dao.deleteMedicine(medicine)
    }
}
