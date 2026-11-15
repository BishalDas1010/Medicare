package com.example.madicare.UI.Medicines

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

// ==========================================
// 1. DATA MODELS & UI STATE
// ==========================================
enum class FilterType(val label: String) {
    ALL("All"),
    ACTIVE("Active"),
    COMPLETED("Completed")
}

data class MedicineItem(
    val id: String,
    val name: String,
    val description: String,
    val remaining: Int,
    val total: Int,
    val bottleDrawable: Int
) {
    val progress: Float get() = if (total > 0) remaining.toFloat() / total.toFloat() else 0f
    val percentage: String get() = "${(progress * 100).toInt()}%"
}

data class MedicinesUiState(
    val medicines: List<MedicineItem> = emptyList(),
    val selectedFilter: FilterType = FilterType.ALL,
    val isLoading: Boolean = false
)

// ==========================================
// 2. VIEWMODEL
// ==========================================
@HiltViewModel
class MedicinesViewModel @Inject constructor(
    private val repository: MedicareRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicinesUiState(isLoading = true))
    val uiState: StateFlow<MedicinesUiState> = _uiState.asStateFlow()

    private var allDbMedicines: List<medicine_details> = emptyList()

    init {
        loadMedicines()
    }

    private fun loadMedicines() {
        viewModelScope.launch {
            repository.medicines.collect { dbMedicines ->
                allDbMedicines = dbMedicines
                updateFilteredMedicines()
            }
        }
    }

    fun onFilterSelected(filter: FilterType) {
        _uiState.update { it.copy(selectedFilter = filter) }
        updateFilteredMedicines()
    }

    private fun updateFilteredMedicines() {
        val allMapped = allDbMedicines.map { item ->
            val bottleRes = if (item.bottleColorIndex != 0) item.bottleColorIndex else com.example.madicare.R.drawable.bluebotttle
            val desc = buildString {
                if (!item.strength.isNullOrBlank()) append(item.strength)
                if (isNotEmpty()) append(" - ")
                append("${item.dosePerTime} ${item.howOften}")
            }
            MedicineItem(
                id = item.mediId.toString(),
                name = item.medicineName ?: "Medicine",
                description = desc,
                remaining = item.remainingTablets,
                total = item.totalTabletsInPack,
                bottleDrawable = bottleRes
            )
        }

        _uiState.update { currentState ->
            val filtered = when (currentState.selectedFilter) {
                FilterType.ALL -> allMapped
                FilterType.ACTIVE -> allMapped.filter { it.remaining > 0 }
                FilterType.COMPLETED -> allMapped.filter { it.remaining <= 0 }
            }
            currentState.copy(medicines = filtered, isLoading = false)
        }
    }
}
