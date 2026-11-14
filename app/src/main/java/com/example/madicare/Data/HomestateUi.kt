package com.example.madicare.Data

import java.util.Date

data class DayModel(
    val dayName: String,
    val dayNumber: String,
    val fullDateString: String,
    val date: Date,
    val isSelected: Boolean
)

data class HomeUiState(
    val dateString: String = "",
    val isToday: Boolean = true,
    val medicinesTaken: Int = 0,
    val totalMedicines: Int = 0,
    val nextTime: String = "None",
    val schedule: List<Medicine> = emptyList(),
    val weekDays: List<DayModel> = emptyList(),
    val isLoading: Boolean = true
) {
    // this is the for the progress bar
    val progressFraction: Float
        get() = if (totalMedicines > 0) medicinesTaken.toFloat() / totalMedicines.toFloat() else 0f
}
