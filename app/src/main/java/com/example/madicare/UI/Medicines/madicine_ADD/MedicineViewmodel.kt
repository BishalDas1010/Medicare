package com.example.madicare.UI.Medicines.madicine_ADD

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

// The state holds everything the UI needs to render
data class AddMedicineUiState(
    val medicineName: String = "",
    val strength: String = "",
    val totalTablets: String = "",
    val dosePerTime: String = "",
    val frequency: String = "Daily",
    val times: List<String> = listOf("8:00 AM", "8:00 PM"),
    val isSaving: Boolean = false,
    val selectedBottleId: Int = 0
)

@HiltViewModel
class AddMedicineViewModel @Inject constructor(
    private val repository: MedicareRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMedicineUiState())
    val uiState: StateFlow<AddMedicineUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) = _uiState.update { it.copy(medicineName = name) }
    fun onStrengthChange(strength: String) = _uiState.update { it.copy(strength = strength) }
    fun onTotalTabletsChange(total: String) = _uiState.update { it.copy(totalTablets = total) }
    fun onDoseChange(dose: String) = _uiState.update { it.copy(dosePerTime = dose) }
    fun onFrequencyChange(freq: String) = _uiState.update { it.copy(frequency = freq) }
    fun onBottleSelected(bottleId: Int) = _uiState.update { it.copy(selectedBottleId = bottleId) }

    fun addTime(time: String) {
        _uiState.update { it.copy(times = it.times + time) }
    }

    fun removeTime(time: String) {
        _uiState.update { it.copy(times = it.times - time) }
    }

    fun onSaveClicked(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.medicineName.isBlank()) {
            return
        }

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val totalTablets = state.totalTablets.toIntOrNull() ?: 30
            val primaryTime = state.times.firstOrNull() ?: "8:00 AM"

            val newMedicine = medicine_details(
                userId = 1L,
                medicineName = state.medicineName,
                strength = if (state.strength.isNotBlank()) state.strength else null,
                totalTabletsInPack = totalTablets,
                remainingTablets = totalTablets,
                dosePerTime = if (state.dosePerTime.isNotBlank()) state.dosePerTime else "1 Tablet",
                howOften = state.frequency,
                time = primaryTime,
                bottleColorIndex = state.selectedBottleId,
                isTaken = false
            )

            repository.insertMedicine(newMedicine)

            _uiState.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }
}
