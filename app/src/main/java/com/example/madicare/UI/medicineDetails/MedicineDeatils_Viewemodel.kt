package com.example.madicare.UI.medicineDetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.madicare.DATABASE.MedicareRepository
import com.example.madicare.DATABASE.medicine_details
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MedicationUiState(
    val medicationName: String = "",
    val dosageInfo: String = "",
    val remainingTablets: Int = 0,
    val totalTablets: Int = 0,
    val startDate: String = "",
    val endDate: String = "",
    val nextDoseTime: String = "",
    val doseSchedule: List<DoseScheduleItem> = emptyList(),
    val refillReminderCount: Int = 5,
    val bottleDrawable: Int = com.example.madicare.R.drawable.bluebotttle
) {
    // Derived state (calculated properties)
    val progress: Float
        get() = if (totalTablets > 0) remainingTablets.toFloat() / totalTablets.toFloat() else 0f

    val progressPercentageText: String
        get() = "${(progress * 100).toInt()}%"
}

data class DoseScheduleItem(
    val time: String,
    val dose: String,
    val frequency: String
)

// Defines all possible user interactions
sealed interface MedicationUiEvent {
    data object OnBackClick : MedicationUiEvent
    data object OnEditClick : MedicationUiEvent
    data object OnDeleteClick : MedicationUiEvent
    data object OnMarkAsTakenClick : MedicationUiEvent
    data object OnAddStockClick : MedicationUiEvent
    data object OnViewHistoryClick : MedicationUiEvent
    data object OnEditRefillReminderClick : MedicationUiEvent
}

@HiltViewModel
class MedicationViewModel @Inject constructor(
    private val repository: MedicareRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val medicineId: String = savedStateHandle.get<String>("medicineId") ?: ""
    private var currentMedicineItem: medicine_details? = null

    private val _uiState = MutableStateFlow(MedicationUiState())
    val uiState: StateFlow<MedicationUiState> = _uiState.asStateFlow()

    init {
        loadMedicineDetails()
    }

    private fun loadMedicineDetails() {
        val id = medicineId.toLongOrNull() ?: 0L
        viewModelScope.launch {
            repository.medicines.collect { list ->
                val item = list.find { it.mediId == id } ?: list.firstOrNull()
                if (item != null) {
                    currentMedicineItem = item
                    val bottleRes = if (item.bottleColorIndex != 0) item.bottleColorIndex else com.example.madicare.R.drawable.bluebotttle
                    val total = item.totalTabletsInPack
                    val remaining = item.remainingTablets
                    val desc = buildString {
                        if (!item.strength.isNullOrBlank()) append("${item.strength} • ")
                        append("${item.dosePerTime} ${item.howOften}")
                    }
                    _uiState.update {
                        it.copy(
                            medicationName = item.medicineName ?: "Medicine",
                            dosageInfo = desc,
                            remainingTablets = remaining,
                            totalTablets = total,
                            startDate = "1 Oct 2026",
                            endDate = "30 Oct 2026",
                            nextDoseTime = item.time,
                            doseSchedule = listOf(DoseScheduleItem(item.time, item.dosePerTime, item.howOften)),
                            refillReminderCount = 5,
                            bottleDrawable = bottleRes
                        )
                    }
                }
            }
        }
    }

    fun updateRefillReminder(count: Int) {
        _uiState.update { it.copy(refillReminderCount = count) }
    }

    fun addStock(amount: Int) {
        _uiState.update { currentState ->
            val newRemaining = currentState.remainingTablets + amount
            currentState.copy(remainingTablets = newRemaining)
        }
    }

    fun handleEvent(event: MedicationUiEvent) {
        val id = medicineId.toLongOrNull() ?: 0L
        when (event) {
            MedicationUiEvent.OnBackClick -> { /* Handled in UI */ }
            MedicationUiEvent.OnEditClick -> { /* Handled in UI */ }
            MedicationUiEvent.OnDeleteClick -> {
                currentMedicineItem?.let { item ->
                    viewModelScope.launch {
                        repository.deleteMedicine(item)
                    }
                }
            }
            MedicationUiEvent.OnMarkAsTakenClick -> {
                if (id != 0L) {
                    viewModelScope.launch {
                        repository.markMedicineAsTaken(id)
                    }
                }
                _uiState.update { currentState ->
                    val newRemaining = (currentState.remainingTablets - 1).coerceAtLeast(0)
                    currentState.copy(remainingTablets = newRemaining)
                }
            }
            MedicationUiEvent.OnAddStockClick -> {
                addStock(10) // Quick add 10 tablets
            }
            MedicationUiEvent.OnViewHistoryClick -> { /* Navigate to History */ }
            MedicationUiEvent.OnEditRefillReminderClick -> { /* Handled in UI */ }
        }
    }
}
