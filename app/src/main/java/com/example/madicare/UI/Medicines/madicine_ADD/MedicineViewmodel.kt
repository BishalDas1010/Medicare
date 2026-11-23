package com.example.madicare.UI.Medicines.madicine_ADD

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
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

// --- Models ---
data class AddMedicineUiState(
    val medicineName: String = "",
    val strength: String = "",
    val dosageForm: String = "Tablet",
    val totalQuantity: String = "30",
    val unit: String = "Tablets",
    val selectedBottleId: Int = com.example.madicare.R.drawable.bluebotttle,
    val selectedColor: Long = 0xFFFFA500, // Orange
    val takeAmount: Int = 1,
    val timesPerDayCount: Int = 3,
    val timesPerDay: String = "3 times a day",
    val frequency: String = "Every day",
    val selectedDays: List<String> = emptyList(),
    val doseTimes: List<String> = listOf("8:00 AM", "2:00 PM", "8:00 PM"),
    val treatmentDurationOption: Int = 0, // 0 = Until runs out, 1 = Set duration
    val startDate: String = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
    val durationDays: Int = 30,
    val notes: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
) {
    val totalDailyConsumption: Int
        get() = takeAmount * timesPerDayCount

    val calculatedEndDate: String
        get() {
            val cal = Calendar.getInstance()
            val totalQty = totalQuantity.toIntOrNull() ?: 30
            val daily = if (totalDailyConsumption > 0) totalDailyConsumption else 1
            val days = if (treatmentDurationOption == 0) (totalQty / daily) else durationDays
            cal.add(Calendar.DAY_OF_YEAR, days)
            return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(cal.time)
        }

    val frequencySummary: String
        get() = when (frequency) {
            "Specific days" -> if (selectedDays.isNotEmpty()) "Specific days: ${selectedDays.joinToString(", ")}" else "Specific days"
            else -> frequency
        }
}

// --- ViewModel ---
@HiltViewModel
class AddMedicineViewModel @Inject constructor(
    private val repository: MedicareRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val medicineId: Long = savedStateHandle.get<Long>("medicineId") ?: -1L
    private var existingRemainingTablets: Int? = null

    private val _uiState = MutableStateFlow(AddMedicineUiState())
    val uiState: StateFlow<AddMedicineUiState> = _uiState.asStateFlow()

    init {
        if (medicineId != -1L) {
            loadExistingMedicine(medicineId)
        }
    }

    private fun loadExistingMedicine(id: Long) {
        viewModelScope.launch {
            repository.medicines.collect { list ->
                val item = list.find { it.mediId == id }
                if (item != null) {
                    existingRemainingTablets = item.remainingTablets
                    val parsedTimes = if (item.doseTimesJson.isNotBlank()) {
                        item.doseTimesJson.split(",").map { it.trim() }
                    } else listOf(item.time)

                    val parsedDays = if (item.selectedDays.isNotBlank()) {
                        item.selectedDays.split(",").map { it.trim() }
                    } else emptyList()

                    _uiState.update {
                        it.copy(
                            medicineName = item.medicineName ?: "",
                            strength = item.strength ?: "",
                            dosageForm = item.dosageForm,
                            totalQuantity = item.totalTabletsInPack.toString(),
                            unit = item.quantityUnit,
                            selectedBottleId = if (item.bottleColorIndex != 0) item.bottleColorIndex else com.example.madicare.R.drawable.bluebotttle,
                            selectedColor = if (item.pillColorHex != 0L) item.pillColorHex else 0xFFFFA500,
                            takeAmount = if (item.takeAmount > 0) item.takeAmount else 1,
                            timesPerDayCount = if (item.timesPerDay > 0) item.timesPerDay else 1,
                            timesPerDay = "${item.timesPerDay} time(s) a day",
                            frequency = item.howOften.split(":").first().trim(),
                            selectedDays = parsedDays,
                            doseTimes = parsedTimes,
                            treatmentDurationOption = item.treatmentDurationOption,
                            startDate = if (item.startDate.isNotBlank()) item.startDate else it.startDate,
                            notes = item.notes
                        )
                    }
                }
            }
        }
    }

    fun onNameChange(name: String) = _uiState.update { it.copy(medicineName = name, errorMessage = null) }
    fun onStrengthChange(strength: String) = _uiState.update { it.copy(strength = strength) }

    fun onDosageFormChange(form: String) {
        val newUnit = when (form) {
            "Tablet" -> "Tablets"
            "Capsule" -> "Capsules"
            "Syrup" -> "ml"
            "Drops" -> "Drops"
            "Injection" -> "Doses"
            else -> "Units"
        }
        _uiState.update { it.copy(dosageForm = form, unit = newUnit) }
    }

    fun onTotalQuantityChange(qty: String) = _uiState.update { it.copy(totalQuantity = qty, errorMessage = null) }
    fun onBottleSelected(bottleId: Int) = _uiState.update { it.copy(selectedBottleId = bottleId) }

    fun onTakeAmountChange(delta: Int) {
        _uiState.update {
            val newAmount = (it.takeAmount + delta).coerceAtLeast(1)
            it.copy(takeAmount = newAmount)
        }
    }

    fun onTimesPerDayChange(count: Int) {
        _uiState.update { currentState ->
            val defaultTimes = listOf("8:00 AM", "2:00 PM", "8:00 PM", "12:00 PM", "6:00 PM", "10:00 PM")
            val currentTimes = currentState.doseTimes.toMutableList()
            if (count > currentTimes.size) {
                for (i in currentTimes.size until count) {
                    currentTimes.add(defaultTimes.getOrElse(i) { "12:00 PM" })
                }
            } else if (count < currentTimes.size && count > 0) {
                while (currentTimes.size > count) {
                    currentTimes.removeAt(currentTimes.lastIndex)
                }
            }
            currentState.copy(
                timesPerDayCount = count,
                timesPerDay = "$count time(s) a day",
                doseTimes = currentTimes
            )
        }
    }

    fun onColorSelected(color: Long) = _uiState.update { it.copy(selectedColor = color) }
    fun onFrequencyChange(freq: String) = _uiState.update { it.copy(frequency = freq) }

    fun toggleDay(day: String) {
        _uiState.update { currentState ->
            val currentDays = currentState.selectedDays.toMutableList()
            if (currentDays.contains(day)) {
                currentDays.remove(day)
            } else {
                currentDays.add(day)
            }
            val orderedDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            currentState.copy(selectedDays = currentDays.sortedBy { orderedDays.indexOf(it) })
        }
    }

    fun addDoseTime(time: String) {
        _uiState.update { currentState ->
            if (currentState.doseTimes.contains(time)) currentState
            else currentState.copy(doseTimes = (currentState.doseTimes + time).sorted())
        }
    }

    fun updateDoseTime(index: Int, newTime: String) {
        _uiState.update { currentState ->
            val list = currentState.doseTimes.toMutableList()
            if (index in list.indices) {
                list[index] = newTime
            }
            currentState.copy(doseTimes = list.sorted())
        }
    }

    fun removeDoseTime(time: String) {
        _uiState.update { it.copy(doseTimes = it.doseTimes - time) }
    }

    fun onTreatmentDurationChange(option: Int) = _uiState.update { it.copy(treatmentDurationOption = option) }
    fun onDurationDaysChange(days: Int) = _uiState.update { it.copy(durationDays = days) }
    fun onNotesChange(notes: String) = _uiState.update { it.copy(notes = notes) }

    fun saveMedicine(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.medicineName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter the medicine name") }
            return
        }
        val totalQty = state.totalQuantity.toIntOrNull() ?: 0
        if (totalQty <= 0) {
            _uiState.update { it.copy(errorMessage = "Total quantity must be greater than zero") }
            return
        }
        if (state.doseTimes.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please configure at least one dose time") }
            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            val primaryTime = state.doseTimes.firstOrNull() ?: "8:00 AM"
            val doseStr = "${state.takeAmount} ${state.dosageForm.lowercase()}"

            val medicine = medicine_details(
                mediId = if (medicineId != -1L) medicineId else 0L,
                userId = 1L,
                medicineName = state.medicineName.trim(),
                strength = if (state.strength.isNotBlank()) state.strength.trim() else null,
                dosageForm = state.dosageForm,
                totalTabletsInPack = totalQty,
                remainingTablets = existingRemainingTablets ?: totalQty,
                quantityUnit = state.unit,
                dosePerTime = doseStr,
                takeAmount = state.takeAmount,
                timesPerDay = state.timesPerDayCount,
                howOften = state.frequencySummary,
                selectedDays = state.selectedDays.joinToString(","),
                time = primaryTime,
                doseTimesJson = state.doseTimes.joinToString(","),
                bottleColorIndex = state.selectedBottleId,
                pillColorHex = state.selectedColor,
                treatmentDurationOption = state.treatmentDurationOption,
                startDate = state.startDate,
                endDate = state.calculatedEndDate,
                notes = state.notes.trim(),
                isTaken = false
            )

            repository.insertMedicine(medicine)
            _uiState.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }
}
