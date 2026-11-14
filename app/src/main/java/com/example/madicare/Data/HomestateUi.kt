package com.example.madicare.Data

data class HomeUiState(
    val dateString: String = "",
    val medicinesTaken: Int = 0,
    val totalMedicines: Int = 0,
    val nextTime: String = "None",
    val schedule: List<Medicine> = emptyList(),
    val isLoading: Boolean = true
) {
    // this is the for the progress bar

    val progressFraction: Float
        get() = if (totalMedicines > 0) medicinesTaken.toFloat() / totalMedicines.toFloat() else 0f
}