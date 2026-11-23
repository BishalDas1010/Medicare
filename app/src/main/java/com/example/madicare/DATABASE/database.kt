package com.example.madicare.DATABASE

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Profile_details"
)
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val person_Frist_nameval: String,
    val person_Last_name: String,
    val profile_photo: Int? = null,
    val gender: String? = null,
    val phoneNumber: String,
    val emailaddress: String,
)

@Entity(
    tableName = "Medicine_details",
    indices = [Index("userId")]
)
data class medicine_details(
    @PrimaryKey(autoGenerate = true)
    val mediId: Long = 0,
    val userId: Long = 1L,
    val medicineName: String? = null,
    val strength: String? = null,
    val dosageForm: String = "Tablet",
    val totalTabletsInPack: Int = 30,
    val remainingTablets: Int = 30,
    val quantityUnit: String = "Tablets",
    val dosePerTime: String = "1 tablet",
    val takeAmount: Int = 1,
    val timesPerDay: Int = 1,
    val howOften: String = "Every day",
    val selectedDays: String = "",
    val time: String = "8:00 AM",
    val doseTimesJson: String = "8:00 AM",
    val bottleColorIndex: Int = com.example.madicare.R.drawable.bluebotttle,
    val pillColorHex: Long = 0xFFFFA500,
    val treatmentDurationOption: Int = 0,
    val startDate: String = "",
    val endDate: String = "",
    val notes: String = "",
    val isTaken: Boolean = false,
    val historyLogsJson: String = ""
)

@Entity(tableName = "health_info")
data class HealthInfoEntity(
    @PrimaryKey val userId: Long,
    val bloodGroup: String?,
    val height: String?,
    val weight: String?,
    val knownAllergies: String?,
    val existingMedicineCondition: String?
)

@Entity(tableName = "doctor_visits")
data class DoctorVisitEntity(
    @PrimaryKey(autoGenerate = true) val visitId: Long = 0,
    val userId: Long,
    val drName: String,
    val specialization: String,
    val hospitalNameClinic: String,
    val drEmail: String?,
    val drPhoneNumber: String?,
    val visitDate: Long,
    val nextVisitDate: Long?,
    val prescriptionUri: String?,
    val drAddress: String?
)

@Entity(tableName = "test_results")
data class TestResultEntity(
    @PrimaryKey val testId: String,
    val testName: String,
    val status: String, // "NOT_TESTED", "RUNNING", "PASSED", "FAILED"
    val executionTime: Long,
    val errorMessage: String? = null
)

@Entity(tableName = "reminder_event_logs")
data class ReminderEventLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val eventType: String,
    val details: String,
    val isSuccess: Boolean
)
