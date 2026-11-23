package com.example.madicare.UI.MedicineDetails

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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

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
    val historyLogs: List<String> = emptyList(),
    val bottleDrawable: Int = com.example.madicare.R.drawable.bluebotttle,
    val pillColorHex: Long = 0xFFFFA500,
    val notes: String = "",
    val isLoading: Boolean = false
) {
    val progressFraction: Float get() = if (total > 0) remaining.toFloat() / total.toFloat() else 0f
    val percentageText: String get() = "${(progressFraction * 100).toInt()}%"
    val isLowStock: Boolean get() = remaining <= 5
}

@HiltViewModel
class MedicineDetailsViewModel @Inject constructor(
    private val repository: MedicareRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val medicineId: String = savedStateHandle.get<String>("medicineId") ?: ""
    private var currentMedicineItem: medicine_details? = null

    private val _uiState = MutableStateFlow(MedicineDetailsUiState(isLoading = true))
    val uiState: StateFlow<MedicineDetailsUiState> = _uiState.asStateFlow()

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
                    val desc = buildString {
                        if (!item.strength.isNullOrBlank()) append("${item.strength} • ")
                        append("${item.dosePerTime} × ${item.timesPerDay} daily")
                    }
                    val doseList = if (item.doseTimesJson.isNotBlank()) {
                        item.doseTimesJson.split(",").map { t ->
                            DoseItem(t.trim(), item.dosePerTime, item.howOften, item.isTaken)
                        }
                    } else {
                        listOf(DoseItem(item.time, item.dosePerTime, item.howOften, item.isTaken))
                    }

                    _uiState.update {
                        it.copy(
                            name = item.medicineName ?: "Medicine",
                            description = desc,
                            remaining = item.remainingTablets,
                            total = item.totalTabletsInPack,
                            startDate = if (item.startDate.isNotBlank()) item.startDate else "Today",
                            endDate = if (item.endDate.isNotBlank()) item.endDate else "30 days",
                            nextDose = item.time,
                            schedule = doseList,
                            bottleDrawable = if (item.bottleColorIndex != 0) item.bottleColorIndex else com.example.madicare.R.drawable.bluebotttle,
                            pillColorHex = if (item.pillColorHex != 0L) item.pillColorHex else 0xFFFFA500,
                            notes = item.notes,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun onBackClick() { /* Handled in UI */ }

    fun onDeleteClick(onDeleted: () -> Unit) {
        currentMedicineItem?.let { item ->
            viewModelScope.launch {
                repository.deleteMedicine(item)
                onDeleted()
            }
        }
    }

    fun onMarkAsTakenClick() {
        val id = medicineId.toLongOrNull() ?: 0L
        if (id != 0L) {
            viewModelScope.launch {
                repository.markMedicineAsTaken(id)
            }
        }
        val timestamp = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        _uiState.update { currentState ->
            val newRemaining = (currentState.remaining - 1).coerceAtLeast(0)
            val updatedHistory = listOf("Taken at $timestamp") + currentState.historyLogs
            currentState.copy(
                remaining = newRemaining,
                historyLogs = updatedHistory
            )
        }
    }

    fun onEditClick() { /* Handled in UI */ }

    fun onRefillClick() {
        currentMedicineItem?.let { item ->
            val newRemaining = item.totalTabletsInPack
            val id = medicineId.toLongOrNull() ?: 0L
            viewModelScope.launch {
                repository.updateRemainingTablets(id, newRemaining)
            }
            val timestamp = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
            _uiState.update {
                it.copy(
                    remaining = newRemaining,
                    historyLogs = listOf("Refilled stock at $timestamp") + it.historyLogs
                )
            }
        }
    }

    fun onHistoryClick() { /* Handled in UI */ }
}
