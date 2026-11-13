package com.example.madicare.UI.MedicineDetails


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DoseItem(
    val time: String,
    val amount: String,
    val frequency: String,
    val isTaken: Boolean = false
)

data class MedicineDetailsUiState(
    val name: String = "",
    val description: String = "",
    val remaining: Int = 0,
    val total: Int = 0,
    val startDate: String = "",
    val endDate: String = "",
    val nextDose: String = "",
    val schedule: List<DoseItem> = emptyList(),
    val isLoading: Boolean = false
) {
    val progressFraction: Float get() = if (total > 0) remaining.toFloat() / total.toFloat() else 0f
    val percentageText: String get() = "${(progressFraction * 100).toInt()}%"
}

class MedicineDetailsViewModel : ViewModel() {
    // In a real app, this state would be populated from a database or API
    private val _uiState = MutableStateFlow(
        MedicineDetailsUiState(
            name = "Paracetamol",
            description = "500 mg - 1 tablet",
            remaining = 20,
            total = 30,
            startDate = "1 Oct 2026",
            endDate = "30 Oct 2026",
            nextDose = "Today, 8:00 PM",
            schedule = listOf(
                DoseItem("8:00 AM", "1 tablet", "Daily"),
                DoseItem("8:00 PM", "1 tablet", "Daily")
            )
        )
    )
    val uiState: StateFlow<MedicineDetailsUiState> = _uiState.asStateFlow()

    // Action handlers (Mock implementations)
    fun onBackClick() { /* Handle navigation back */ }
    fun onDeleteClick() { /* Handle delete */ }
    fun onMarkAsTakenClick() { /* Update remaining logic */ }
    fun onEditClick() { /* Navigate to edit screen */ }
    fun onRefillClick() { /* Navigate to refill screen */ }
    fun onHistoryClick() { /* Navigate to history screen */ }
}