package com.example.madicare.DATABASE

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// --- DAOs (Data Access Objects) ---
// WHY: DAOs are interfaces that define how we interact with the database. We don't write SQL queries in the UI or ViewModel.
// HOW IT CONNECTS: The Repository calls these methods. Room generates the actual SQL code behind the scenes.
@Dao
interface MedicareDao {
    // WHY: Returns a Flow<ProfileEntity?>. Flow is an asynchronous stream. If the database updates, the Flow emits the new data automatically.
    // HOW IT CONNECTS: The Repository collects this Flow, which the ViewModel observes, which updates the UI.
    @Query("SELECT * FROM profile_details WHERE id = :userId LIMIT 1")
    fun getProfile(userId: Long): Flow<ProfileEntity?>

    // WHAT: Fetches all medicines for a user, ordered by time.
    @Query("SELECT * FROM medicine_details WHERE userId = :userId ORDER BY time ASC")
    fun getMedicines(userId: Long): Flow<List<medicine_details>>

    @Query("SELECT * FROM health_info WHERE userId = :userId LIMIT 1")
    fun getHealthInfo(userId: Long): Flow<HealthInfoEntity?>

    @Query("SELECT * FROM doctor_visits WHERE userId = :userId ORDER BY visitDate DESC")
    fun getDoctorVisits(userId: Long): Flow<List<DoctorVisitEntity>>

    // WHY: @Insert with REPLACE strategy means if an item with the same Primary Key exists, it will overwrite it instead of crashing.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity): Long // Returns the newly generated ID

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: medicine_details): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthInfo(healthInfo: HealthInfoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctorVisit(visit: DoctorVisitEntity)

    @Query("UPDATE Medicine_details SET isTaken = :isTaken WHERE mediId = :medicineId")
    suspend fun updateMedicineTakenStatus(medicineId: Long, isTaken: Boolean)

    @Query("UPDATE Medicine_details SET remainingTablets = :remaining WHERE mediId = :medicineId")
    suspend fun updateRemainingTablets(medicineId: Long, remaining: Int)

    @androidx.room.Delete
    suspend fun deleteMedicine(medicine: medicine_details)

    // --- Test Results ---
    @Query("SELECT * FROM test_results")
    fun getAllTestResults(): Flow<List<TestResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTestResult(result: TestResultEntity)

    @Query("DELETE FROM test_results")
    suspend fun clearTestResults()

    // --- Reminder Event Logs ---
    @Query("SELECT * FROM reminder_event_logs ORDER BY timestamp DESC")
    fun getAllReminderEventLogs(): Flow<List<ReminderEventLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventLog(log: ReminderEventLogEntity)

    @Query("DELETE FROM reminder_event_logs")
    suspend fun clearEventLogs()
}