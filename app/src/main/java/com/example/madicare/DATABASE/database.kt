package com.example.madicare.DATABASE

import androidx.room.Entity
import androidx.room.ForeignKey
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
    foreignKeys = [ForeignKey(
        entity = ProfileEntity::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)
data class medicine_details(
    @PrimaryKey(autoGenerate = true)
    val mediId: Long = 0,
    val userId: Long,
    val medicineName: String?,
    val strength: String?,
    val totalTabletsInPack: Int,
    val remainingTablets: Int = totalTabletsInPack,
    val dosePerTime: String,
    val howOften: String,
    val time: String,
    val bottleColorIndex: Int = 0,
    val isTaken: Boolean = false
)

@Entity(tableName = "health_info")
data class HealthInfoEntity(
    // WHY: We use userId as the PrimaryKey here (not autoGenerate) because it's a 1-to-1 relationship. One user = One health record.
    @PrimaryKey val userId: Long,
    val bloodGroup: String?,
    val height: String?,
    val weight: String?,
    val knownAllergies: String?,
    val existingMedicineCondition: String?
)

@Entity(
    tableName = "doctor_visits",
    foreignKeys = [ForeignKey(
        entity = ProfileEntity::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("userId")]
)
data class DoctorVisitEntity(
    @PrimaryKey(autoGenerate = true) val visitId: Long = 0,
    val userId: Long, // WHAT: Links this visit to the specific user
    val drName: String, // Note: Changed from PK in your diagram to a normal column to allow multiple visits to same doctor
    val specialization: String,
    val hospitalNameClinic: String,
    val drEmail: String?,
    val drPhoneNumber: String?,
    val visitDate: Long,
    val nextVisitDate: Long?,
    val prescriptionUri: String?,
    val drAddress: String?
)
