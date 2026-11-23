package com.example.madicare.UI.ReminderTest

enum class TestStatus(val label: String) {
    NOT_TESTED("NOT TESTED"),
    RUNNING("RUNNING"),
    PASSED("PASSED"),
    FAILED("FAILED")
}

data class TestCase(
    val id: String,
    val name: String,
    val description: String,
    val status: TestStatus = TestStatus.NOT_TESTED,
    val executionTimeMs: Long = 0L,
    val errorMessage: String? = null
)

data class SummaryStats(
    val total: Int = 12,
    val passed: Int = 0,
    val failed: Int = 0,
    val notTested: Int = 12,
    val successPercentage: Int = 0,
    val lastExecutionTime: String = "Never"
)

data class LiveReminderItem(
    val medicineId: Long,
    val medicineName: String,
    val scheduledTimeFormatted: String,
    val preReminderTimeFormatted: String?,
    val alarmType: String,
    val nextTriggerTimeFormatted: String,
    val status: String,
    val snoozeCount: Int,
    val pendingIntentId: Int
)

data class EventLogItem(
    val id: Long,
    val timestampFormatted: String,
    val eventType: String,
    val details: String,
    val isSuccess: Boolean
)
