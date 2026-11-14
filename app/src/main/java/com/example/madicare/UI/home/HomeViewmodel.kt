package com.example.madicare.UI.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.madicare.DATABASE.MedicareRepository
import com.example.madicare.DATABASE.medicine_details
import com.example.madicare.Data.DayModel
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
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MedicareRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var selectedDate: Date = Date()
    private var allDbMedicines: List<medicine_details> = emptyList()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            repository.medicines.collect { dbMedicines ->
                allDbMedicines = dbMedicines
                updateUiState()
            }
        }
    }

    fun selectDate(date: Date) {
        selectedDate = date
        updateUiState()
    }

    private fun updateUiState() {
        val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
        val dateStr = dateFormat.format(selectedDate)

        val calendarSelected = Calendar.getInstance().apply { time = selectedDate }
        val calendarToday = Calendar.getInstance()
        val isToday = calendarSelected.get(Calendar.YEAR) == calendarToday.get(Calendar.YEAR) &&
                calendarSelected.get(Calendar.DAY_OF_YEAR) == calendarToday.get(Calendar.DAY_OF_YEAR)

        val weekDays = generateWeekDays(selectedDate)

        var foundNext = false
        val mappedSchedule = allDbMedicines.map { item ->
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

        val takenCount = allDbMedicines.count { it.isTaken }
        val totalCount = allDbMedicines.size

        val nextMedicineTime = allDbMedicines.firstOrNull { !it.isTaken }?.time
            ?: if (allDbMedicines.isNotEmpty()) "All Taken" else "None"

        _uiState.update {
            it.copy(
                dateString = dateStr,
                isToday = isToday,
                medicinesTaken = takenCount,
                totalMedicines = totalCount,
                nextTime = nextMedicineTime,
                schedule = mappedSchedule,
                weekDays = weekDays,
                isLoading = false
            )
        }
    }

    private fun generateWeekDays(selected: Date): List<DayModel> {
        val calendar = Calendar.getInstance().apply { time = selected }
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        val days = mutableListOf<DayModel>()
        val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
        val dayNameFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dayNumFormat = SimpleDateFormat("d", Locale.getDefault())

        val selectedCal = Calendar.getInstance().apply { time = selected }

        for (i in 0..6) {
            val currentDate = calendar.time
            val dayName = dayNameFormat.format(currentDate)
            val dayNum = dayNumFormat.format(currentDate)
            val fullDateStr = dateFormat.format(currentDate)

            val isSelected = calendar.get(Calendar.YEAR) == selectedCal.get(Calendar.YEAR) &&
                    calendar.get(Calendar.DAY_OF_YEAR) == selectedCal.get(Calendar.DAY_OF_YEAR)

            days.add(DayModel(dayName, dayNum, fullDateStr, currentDate, isSelected))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return days
    }

    fun markAsTaken(medicineIdStr: String) {
        val medicineId = medicineIdStr.toLongOrNull() ?: return
        viewModelScope.launch {
            repository.markMedicineAsTaken(medicineId)
        }
    }
}
