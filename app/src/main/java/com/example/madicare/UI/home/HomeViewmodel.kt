package com.example.madicare.UI.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.madicare.DATABASE.AppDatabase
import com.example.madicare.DATABASE.MedicareRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- Data Models ---
enum class MedicineStatus {
    TAKEN, NEXT, UPCOMING
}

data class Medicine(
    val id: String,
    val name: String,
    val dosage: String,
    val time: String,
    val status: MedicineStatus
)

// --- UI State ---
data class HomeUiState(
    val dateString: String = "Sun, Nov 16, 2026",
    val medicinesTaken: Int = 0,
    val totalMedicines: Int = 0,
    val nextTime: String = "None",
    val schedule: List<Medicine> = emptyList(),
    val isLoading: Boolean = false
) {
    val progressFraction: Float
        get() = if (totalMedicines > 0) medicinesTaken.toFloat() / totalMedicines.toFloat() else 0f
}

// --- ViewModel ---
class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MedicareRepository(AppDatabase.getDatabase(application).medicareDao())

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            repository.ensureDefaultProfile(1L)
            repository.getMedicines(1L).collect { dbMedicines ->
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
            repository.updateMedicineTakenStatus(medicineId, true)
        }
    }
}
