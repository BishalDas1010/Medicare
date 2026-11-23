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

    // --- Testing & Event Logging ---
    val testResults: Flow<List<TestResultEntity>> = dao.getAllTestResults()
    val eventLogs: Flow<List<ReminderEventLogEntity>> = dao.getAllReminderEventLogs()

    suspend fun saveTestResult(testId: String, testName: String, status: String, executionTime: Long, errorMessage: String? = null) {
        dao.insertOrUpdateTestResult(
            TestResultEntity(
                testId = testId,
                testName = testName,
                status = status,
                executionTime = executionTime,
                errorMessage = errorMessage
            )
        )
    }

    suspend fun logEvent(eventType: String, details: String, isSuccess: Boolean = true) {
        dao.insertEventLog(
            ReminderEventLogEntity(
                timestamp = System.currentTimeMillis(),
                eventType = eventType,
                details = details,
                isSuccess = isSuccess
            )
        )
    }

    suspend fun clearTestResultsAndLogs() {
        dao.clearTestResults()
        dao.clearEventLogs()
    }

    suspend fun updateMedicineTaken(medicineId: Long, isTaken: Boolean) {
        dao.updateMedicineTakenStatus(medicineId, isTaken)
    }
}
