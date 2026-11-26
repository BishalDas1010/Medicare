package com.example.madicare.UI.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.madicare.DATABASE.MedicareRepository
import com.example.madicare.DATABASE.medicine_details
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val repository: MedicareRepository
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(ReportPeriod.WEEK)
    val selectedPeriod: StateFlow<ReportPeriod> = _selectedPeriod.asStateFlow()

    private val _reportData = MutableStateFlow(sampleReportData(ReportPeriod.WEEK))
    val reportData: StateFlow<ReportData> = _reportData.asStateFlow()

    init {
        viewModelScope.launch {
            combine(repository.medicines, _selectedPeriod) { medicines, period ->
                calculateReportData(medicines, period)
            }.collect { data ->
                _reportData.value = data
            }
        }
    }

    fun selectPeriod(period: ReportPeriod) {
        _selectedPeriod.value = period
    }

    private fun calculateReportData(
        medicines: List<medicine_details>,
        period: ReportPeriod
    ): ReportData {
        if (medicines.isEmpty()) {
            return sampleReportData(period)
        }

        val totalMeds = medicines.size
        val takenMeds = medicines.count { it.isTaken }
        val missedMeds = totalMeds - takenMeds

        val multiplier = when (period) {
            ReportPeriod.WEEK -> 7
            ReportPeriod.MONTH -> 30
            ReportPeriod.YEAR -> 365
        }

        val periodLabel = when (period) {
            ReportPeriod.WEEK -> "This week"
            ReportPeriod.MONTH -> "This month"
            ReportPeriod.YEAR -> "This year"
        }

        val compareLabel = when (period) {
            ReportPeriod.WEEK -> "vs last week"
            ReportPeriod.MONTH -> "vs last month"
            ReportPeriod.YEAR -> "vs last year"
        }

        val labels = when (period) {
            ReportPeriod.WEEK -> listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            ReportPeriod.MONTH -> listOf("W1", "W2", "W3", "W4")
            ReportPeriod.YEAR -> listOf("J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")
        }

        val overallPercent = if (totalMeds > 0) ((takenMeds * 100f) / totalMeds).toInt() else 0

        val points = generatePoints(labels.size, overallPercent)

        val factor = (multiplier / 7).coerceAtLeast(1)
        val totalTakenDoses = takenMeds * factor
        val totalMissedDoses = missedMeds * factor

        val medicineAdherenceList = medicines.map { med ->
            val totalForMed = factor * med.timesPerDay
            val takenForMed = if (med.isTaken) totalForMed else (totalForMed * 0.7f).toInt()
            MedicineAdherence(
                name = med.medicineName ?: "Medicine",
                taken = takenForMed,
                total = totalForMed.coerceAtLeast(1)
            )
        }

        val streak = if (overallPercent >= 80) 7 else if (overallPercent >= 50) 3 else 1
        val bestStreak = (streak * 2 + 5).coerceAtLeast(12)

        val insight = when {
            overallPercent >= 90 -> "Outstanding adherence! Your medication routine is well established across all scheduled doses."
            overallPercent >= 70 -> "Good progress! Most doses are taken on schedule. Consider setting pre-reminders for weekend doses."
            overallPercent > 0 -> "Some doses were missed recently. Setting a reminder 15 minutes before your dosage time can help boost adherence."
            else -> "No active medication logs recorded yet. Start logging your doses to see real-time health reports."
        }

        return ReportData(
            adherence = overallPercent,
            trend = if (overallPercent >= 75) 5 else -3,
            compareLabel = compareLabel,
            periodLabel = periodLabel,
            labels = labels,
            points = points,
            taken = totalTakenDoses,
            missed = totalMissedDoses,
            streak = streak,
            bestStreak = bestStreak,
            medicines = medicineAdherenceList,
            insight = insight
        )
    }

    private fun generatePoints(count: Int, basePercent: Int): List<Float> {
        val baseFraction = (basePercent / 100f).coerceIn(0.1f, 1.0f)
        val variations = listOf(0.05f, -0.08f, 0.10f, -0.05f, 0.08f, -0.03f, 0.06f, -0.04f, 0.09f, -0.07f, 0.04f, 0.02f)
        return List(count) { i ->
            val varFactor = variations[i % variations.size]
            (baseFraction + varFactor).coerceIn(0.2f, 1.0f)
        }
    }
}
