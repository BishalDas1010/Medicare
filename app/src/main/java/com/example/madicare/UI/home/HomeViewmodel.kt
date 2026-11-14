package com.example.madicare.UI.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.madicare.DATABASE.MedicareRepository
import com.example.madicare.Data.HomeUiState
import com.example.madicare.Data.Medicine
import com.example.madicare.Ennum.MedicineStatus
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

// --- Data Models ---

// --- ViewModel ---
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MedicareRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // WHAT: We no longer insert dummy data. We just start listening to the empty database.
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            repository.medicines.collect { dbMedicines ->
                val currentDateStr = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()).format(Date())

                var foundNext = false
                val mappedSchedule = dbMedicines.map { item ->
                    val status = when {
                        item.isTaken -> MedicineStatus.TAKEN
                        !foundNext -> {
                            foundNext = true
                            MedicineStatus.NEXT
                        }
                        else -> MedicineStatus.UPCOMING
                    }
                    Medicine(
                        id = item.mediId.toString(),
                        name = item.medicineName ?: "Medicine",
                        dosage = "${item.dosePerTime} • ${item.howOften}",
                        time = item.time,
                        status = status
                    )
                }

                val takenCount = dbMedicines.count { it.isTaken }
                val totalCount = dbMedicines.size

                // If the list is empty, nextTime is "None". If all are taken, it's "All Taken".
                val nextMedicineTime = dbMedicines.firstOrNull { !it.isTaken }?.time
                    ?: if (dbMedicines.isNotEmpty()) "All Taken" else "None"

                _uiState.update {
                    it.copy(
                        dateString = currentDateStr,
                        medicinesTaken = takenCount,
                        totalMedicines = totalCount,
                        nextTime = nextMedicineTime,
                        schedule = mappedSchedule,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun markAsTaken(medicineIdStr: String) {
        val medicineId = medicineIdStr.toLongOrNull() ?: return
        viewModelScope.launch {
            repository.markMedicineAsTaken(medicineId)
        }
    }
}