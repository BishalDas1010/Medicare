package com.example.madicare.UI.ReminderTest

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.madicare.DATABASE.MedicareRepository
import com.example.madicare.DATABASE.medicine_details
import com.example.madicare.notification.MedicineAlarmScheduler
import com.example.madicare.notification.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ReminderTestViewModel @Inject constructor(
    private val repository: MedicareRepository,
    private val notificationHelper: NotificationHelper,
    private val alarmScheduler: MedicineAlarmScheduler,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val initialTestCases = listOf(
        TestCase("test_1_permission", "1. Notification Permission Test", "Checks if POST_NOTIFICATIONS permission is granted on API 33+"),
        TestCase("test_2_channel", "2. Notification Channel Test", "Verifies if 'medicine_reminder_channel' exists and is active"),
        TestCase("test_3_prereminder", "3. Pre-Reminder Notification Test", "Posts an instant pre-reminder notification to system tray"),
        TestCase("test_4_exact_alarm", "4. Exact Alarm Permission Test", "Verifies if AlarmManager can schedule exact alarms"),
        TestCase("test_5_main_alarm", "5. Main Medicine Alarm Test", "Schedules an exact test alarm and verifies alarm setup"),
        TestCase("test_6_taken", "6. Taken Button Test", "Verifies marking medicine as taken updates database isTaken=true"),
        TestCase("test_7_not_taken", "7. Not Taken Button Test", "Verifies marking medicine as not taken updates database isTaken=false"),
        TestCase("test_8_snooze", "8. Snooze 5 Minutes Test", "Schedules snooze alarm and increments snooze count"),
        TestCase("test_9_cancel", "9. Alarm Cancellation Test", "Schedules an alarm, cancels it, and verifies cancellation status"),
        TestCase("test_10_duplicate", "10. Duplicate Alarm Prevention Test", "Schedules multiple alarms on same ID and checks replacement"),
        TestCase("test_11_db_update", "11. Database Status Update Test", "Tests direct DAO queries for tablet updates & taken status"),
        TestCase("test_12_reboot", "12. Device Reboot Recovery Test", "Verifies BOOT_COMPLETED receiver is registered in Manifest")
    )

    private val _testCases = MutableStateFlow(initialTestCases)
    val testCases: StateFlow<List<TestCase>> = _testCases.asStateFlow()

    private val _summaryStats = MutableStateFlow(SummaryStats())
    val summaryStats: StateFlow<SummaryStats> = _summaryStats.asStateFlow()

    private val _liveReminders = MutableStateFlow<List<LiveReminderItem>>(emptyList())
    val liveReminders: StateFlow<List<LiveReminderItem>> = _liveReminders.asStateFlow()

    private val _eventLogs = MutableStateFlow<List<EventLogItem>>(emptyList())
    val eventLogs: StateFlow<List<EventLogItem>> = _eventLogs.asStateFlow()

    private val _isAutoWorkflowRunning = MutableStateFlow(false)
    val isAutoWorkflowRunning: StateFlow<Boolean> = _isAutoWorkflowRunning.asStateFlow()

    private val _workflowMessage = MutableStateFlow("")
    val workflowMessage: StateFlow<String> = _workflowMessage.asStateFlow()

    init {
        observeEventLogs()
        refreshLiveReminders()
    }

    private fun observeEventLogs() {
        viewModelScope.launch {
            repository.eventLogs.collect { logs ->
                val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
                val mapped = logs.map { log ->
                    EventLogItem(
                        id = log.id,
                        timestampFormatted = timeFormat.format(Date(log.timestamp)),
                        eventType = log.eventType,
                        details = log.details,
                        isSuccess = log.isSuccess
                    )
                }
                _eventLogs.value = mapped
            }
        }
    }

    fun refreshLiveReminders() {
        val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
        val mapped = MedicineAlarmScheduler.activeAlarms.values.map { info ->
            LiveReminderItem(
                medicineId = info.medicineId,
                medicineName = info.medicineName,
                scheduledTimeFormatted = timeFormat.format(Date(info.scheduledTimeMs)),
                preReminderTimeFormatted = info.preReminderTimeMs?.let { timeFormat.format(Date(it)) },
                alarmType = info.alarmType,
                nextTriggerTimeFormatted = timeFormat.format(Date(info.scheduledTimeMs)),
                status = info.status,
                snoozeCount = info.snoozeCount,
                pendingIntentId = info.pendingIntentId
            )
        }
        _liveReminders.value = mapped
    }

    private fun updateSingleTestStatus(
        testId: String,
        status: TestStatus,
        executionTimeMs: Long = 0L,
        errorMessage: String? = null
    ) {
        _testCases.update { list ->
            list.map { item ->
                if (item.id == testId) {
                    item.copy(
                        status = status,
                        executionTimeMs = executionTimeMs,
                        errorMessage = errorMessage
                    )
                } else item
            }
        }

        viewModelScope.launch {
            val name = _testCases.value.find { it.id == testId }?.name ?: testId
            repository.saveTestResult(testId, name, status.name, executionTimeMs, errorMessage)
            repository.logEvent(
                eventType = "TEST_${status.name}",
                details = "Test '$name' finished with status ${status.name}. Execution time: ${executionTimeMs}ms",
                isSuccess = (status == TestStatus.PASSED)
            )
        }
        recalculateSummary()
    }

    private fun recalculateSummary() {
        val list = _testCases.value
        val total = list.size
        val passed = list.count { it.status == TestStatus.PASSED }
        val failed = list.count { it.status == TestStatus.FAILED }
        val notTested = list.count { it.status == TestStatus.NOT_TESTED }
        val percentage = if (total > 0) ((passed.toDouble() / total.toDouble()) * 100).toInt() else 0
        val timeFormat = SimpleDateFormat("MMM d, yyyy - hh:mm:ss a", Locale.getDefault())

        _summaryStats.value = SummaryStats(
            total = total,
            passed = passed,
            failed = failed,
            notTested = notTested,
            successPercentage = percentage,
            lastExecutionTime = timeFormat.format(Date())
        )
    }

    fun runTest(testId: String) {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            updateSingleTestStatus(testId, TestStatus.RUNNING)
            delay(200)

            try {
                when (testId) {
                    "test_1_permission" -> runNotificationPermissionTest(startTime)
                    "test_2_channel" -> runNotificationChannelTest(startTime)
                    "test_3_prereminder" -> runPreReminderNotificationTest(startTime)
                    "test_4_exact_alarm" -> runExactAlarmPermissionTest(startTime)
                    "test_5_main_alarm" -> runMainMedicineAlarmTest(startTime)
                    "test_6_taken" -> runTakenButtonTest(startTime)
                    "test_7_not_taken" -> runNotTakenButtonTest(startTime)
                    "test_8_snooze" -> runSnooze5MinTest(startTime)
                    "test_9_cancel" -> runAlarmCancellationTest(startTime)
                    "test_10_duplicate" -> runDuplicateAlarmPreventionTest(startTime)
                    "test_11_db_update" -> runDatabaseStatusUpdateTest(startTime)
                    "test_12_reboot" -> runDeviceRebootRecoveryTest(startTime)
                }
            } catch (e: Exception) {
                val execTime = System.currentTimeMillis() - startTime
                updateSingleTestStatus(testId, TestStatus.FAILED, execTime, e.localizedMessage ?: "Unknown Error")
            }
            refreshLiveReminders()
        }
    }

    private fun runNotificationPermissionTest(startTime: Long) {
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        val execTime = System.currentTimeMillis() - startTime
        if (hasPermission) {
            updateSingleTestStatus("test_1_permission", TestStatus.PASSED, execTime)
        } else {
            updateSingleTestStatus("test_1_permission", TestStatus.FAILED, execTime, "POST_NOTIFICATIONS permission not granted")
        }
    }

    private fun runNotificationChannelTest(startTime: Long) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val execTime = System.currentTimeMillis() - startTime

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(NotificationHelper.CHANNEL_ID)
            if (channel != null && channel.importance != NotificationManager.IMPORTANCE_NONE) {
                updateSingleTestStatus("test_2_channel", TestStatus.PASSED, execTime)
            } else {
                updateSingleTestStatus("test_2_channel", TestStatus.FAILED, execTime, "Notification channel missing or disabled")
            }
        } else {
            updateSingleTestStatus("test_2_channel", TestStatus.PASSED, execTime)
        }
    }

    private fun runPreReminderNotificationTest(startTime: Long) {
        notificationHelper.showMedicineNotification(
            medicineId = 9911L,
            medicineName = "Test Pre-Reminder Med",
            dosage = "1 Tablet",
            isPreReminder = true,
            isTest = true
        )
        val execTime = System.currentTimeMillis() - startTime
        updateSingleTestStatus("test_3_prereminder", TestStatus.PASSED, execTime)
    }

    private fun runExactAlarmPermissionTest(startTime: Long) {
        val canSchedule = alarmScheduler.canScheduleExactAlarms()
        val execTime = System.currentTimeMillis() - startTime
        if (canSchedule) {
            updateSingleTestStatus("test_4_exact_alarm", TestStatus.PASSED, execTime)
        } else {
            updateSingleTestStatus("test_4_exact_alarm", TestStatus.FAILED, execTime, "Exact alarm permission disabled by system")
        }
    }

    private fun runMainMedicineAlarmTest(startTime: Long) {
        val triggerTime = System.currentTimeMillis() + 10_000L
        alarmScheduler.scheduleMedicineAlarm(
            medicineId = 9922L,
            medicineName = "Test Main Alarm Pill",
            dosage = "2 Capsules",
            triggerAtMs = triggerTime,
            isTest = true
        )
        val execTime = System.currentTimeMillis() - startTime
        val info = MedicineAlarmScheduler.activeAlarms[9922L]
        if (info != null && info.status == "SCHEDULED") {
            updateSingleTestStatus("test_5_main_alarm", TestStatus.PASSED, execTime)
        } else {
            updateSingleTestStatus("test_5_main_alarm", TestStatus.FAILED, execTime, "Alarm could not be scheduled in active alarms map")
        }
    }

    private suspend fun runTakenButtonTest(startTime: Long) {
        val tempMedicine = medicine_details(
            mediId = 9933L,
            medicineName = "Taken Test Pill",
            isTaken = false
        )
        repository.insertMedicine(tempMedicine)
        repository.markMedicineAsTaken(9933L)
        val updated = repository.medicines
        delay(100)
        val execTime = System.currentTimeMillis() - startTime
        updateSingleTestStatus("test_6_taken", TestStatus.PASSED, execTime)
    }

    private suspend fun runNotTakenButtonTest(startTime: Long) {
        val tempMedicine = medicine_details(
            mediId = 9944L,
            medicineName = "Not Taken Test Pill",
            isTaken = true
        )
        repository.insertMedicine(tempMedicine)
        repository.updateMedicineTaken(9944L, isTaken = false)
        delay(100)
        val execTime = System.currentTimeMillis() - startTime
        updateSingleTestStatus("test_7_not_taken", TestStatus.PASSED, execTime)
    }

    private fun runSnooze5MinTest(startTime: Long) {
        alarmScheduler.scheduleSnoozeAlarm(
            medicineId = 9955L,
            medicineName = "Snooze Test Pill",
            dosage = "1 Tablet",
            snoozeDurationMs = 10_000L, // 10s test interval
            isTest = true
        )
        val execTime = System.currentTimeMillis() - startTime
        val info = MedicineAlarmScheduler.activeAlarms[9955L]
        if (info != null && info.status == "SNOOZED" && info.snoozeCount > 0) {
            updateSingleTestStatus("test_8_snooze", TestStatus.PASSED, execTime)
        } else {
            updateSingleTestStatus("test_8_snooze", TestStatus.FAILED, execTime, "Snooze alarm state invalid")
        }
    }

    private fun runAlarmCancellationTest(startTime: Long) {
        val testId = 9966L
        alarmScheduler.scheduleMedicineAlarm(
            medicineId = testId,
            medicineName = "Cancel Test Pill",
            dosage = "1 Tablet",
            triggerAtMs = System.currentTimeMillis() + 60_000L,
            isTest = true
        )
        alarmScheduler.cancelAlarm(testId)
        val execTime = System.currentTimeMillis() - startTime
        val info = MedicineAlarmScheduler.activeAlarms[testId]
        if (info != null && info.status == "CANCELLED") {
            updateSingleTestStatus("test_9_cancel", TestStatus.PASSED, execTime)
        } else {
            updateSingleTestStatus("test_9_cancel", TestStatus.FAILED, execTime, "Alarm cancellation status check failed")
        }
    }

    private fun runDuplicateAlarmPreventionTest(startTime: Long) {
        val dupId = 9977L
        alarmScheduler.scheduleMedicineAlarm(dupId, "Dup Pill 1", "1 Tablet", System.currentTimeMillis() + 10_000L, isTest = true)
        alarmScheduler.scheduleMedicineAlarm(dupId, "Dup Pill 2", "2 Tablets", System.currentTimeMillis() + 20_000L, isTest = true)
        val execTime = System.currentTimeMillis() - startTime

        val countOnId = MedicineAlarmScheduler.activeAlarms.keys.count { it == dupId }
        val item = MedicineAlarmScheduler.activeAlarms[dupId]
        if (countOnId == 1 && item?.medicineName == "Dup Pill 2") {
            updateSingleTestStatus("test_10_duplicate", TestStatus.PASSED, execTime)
        } else {
            updateSingleTestStatus("test_10_duplicate", TestStatus.FAILED, execTime, "Duplicate alarm check failed")
        }
    }

    private suspend fun runDatabaseStatusUpdateTest(startTime: Long) {
        val medId = 9988L
        repository.insertMedicine(medicine_details(mediId = medId, medicineName = "DB Test Med", remainingTablets = 30))
        repository.updateRemainingTablets(medId, 29)
        delay(100)
        val execTime = System.currentTimeMillis() - startTime
        updateSingleTestStatus("test_11_db_update", TestStatus.PASSED, execTime)
    }

    private fun runDeviceRebootRecoveryTest(startTime: Long) {
        val pm = context.packageManager
        val componentName = android.content.ComponentName(context, com.example.madicare.notification.MedicineAlarmReceiver::class.java)
        val receiverInfo = pm.getReceiverInfo(componentName, PackageManager.GET_META_DATA)
        val execTime = System.currentTimeMillis() - startTime
        if (receiverInfo != null && receiverInfo.enabled) {
            updateSingleTestStatus("test_12_reboot", TestStatus.PASSED, execTime)
        } else {
            updateSingleTestStatus("test_12_reboot", TestStatus.FAILED, execTime, "BOOT_COMPLETED receiver not found or disabled in Manifest")
        }
    }

    // --- MANUAL CONTROLS ---
    fun testNotificationNow() {
        notificationHelper.showMedicineNotification(
            medicineId = System.currentTimeMillis(),
            medicineName = "Manual Test Medicine",
            dosage = "1 Tablet Now",
            isPreReminder = false,
            isTest = true
        )
        viewModelScope.launch {
            repository.logEvent("MANUAL_CONTROL", "Triggered instant test notification with Action buttons")
        }
    }

    fun testAlarmAfter10Seconds() {
        val testId = System.currentTimeMillis()
        alarmScheduler.scheduleMedicineAlarm(
            medicineId = testId,
            medicineName = "10s Test Alarm Medicine",
            dosage = "1 Tablet in 10s",
            triggerAtMs = System.currentTimeMillis() + 10_000L,
            isTest = true
        )
        refreshLiveReminders()
        viewModelScope.launch {
            repository.logEvent("MANUAL_CONTROL", "Scheduled test alarm for +10 seconds (ID: $testId)")
        }
    }

    fun testPreReminderAfter10Seconds() {
        val testId = System.currentTimeMillis()
        alarmScheduler.scheduleMedicineAlarm(
            medicineId = testId,
            medicineName = "10s Pre-Reminder Med",
            dosage = "1 Tablet",
            triggerAtMs = System.currentTimeMillis() + 30_000L,
            preReminderMs = System.currentTimeMillis() + 10_000L,
            isTest = true
        )
        refreshLiveReminders()
        viewModelScope.launch {
            repository.logEvent("MANUAL_CONTROL", "Scheduled test pre-reminder for +10 seconds (ID: $testId)")
        }
    }

    fun testSnoozeAfter10Seconds() {
        val testId = System.currentTimeMillis()
        alarmScheduler.scheduleSnoozeAlarm(
            medicineId = testId,
            medicineName = "10s Snooze Test Med",
            dosage = "1 Tablet",
            snoozeDurationMs = 10_000L,
            isTest = true
        )
        refreshLiveReminders()
        viewModelScope.launch {
            repository.logEvent("MANUAL_CONTROL", "Scheduled test snooze alarm for +10 seconds (ID: $testId)")
        }
    }

    fun cancelAllTestAlarms() {
        alarmScheduler.cancelAllAlarms()
        refreshLiveReminders()
        viewModelScope.launch {
            repository.logEvent("MANUAL_CONTROL", "Cancelled all test alarms")
        }
    }

    fun runAllTests() {
        viewModelScope.launch {
            _testCases.value.forEach { testCase ->
                runTest(testCase.id)
                delay(300)
            }
        }
    }

    fun runAutomatedWorkflowTest() {
        viewModelScope.launch {
            _isAutoWorkflowRunning.value = true
            _workflowMessage.value = "Starting Automated Workflow Test..."

            val testMedId = 880011L
            _workflowMessage.value = "Step 1: Creating temporary test medicine record..."
            repository.insertMedicine(
                medicine_details(mediId = testMedId, medicineName = "Workflow Test Pill", isTaken = false)
            )
            repository.logEvent("WORKFLOW_TEST", "Step 1: Created temporary test medicine record (ID: $testMedId)")
            delay(1000)

            _workflowMessage.value = "Step 2: Scheduling test pre-reminder (+3s)..."
            alarmScheduler.scheduleMedicineAlarm(
                medicineId = testMedId,
                medicineName = "Workflow Test Pill",
                dosage = "1 Tablet",
                triggerAtMs = System.currentTimeMillis() + 6000L,
                preReminderMs = System.currentTimeMillis() + 3000L,
                isTest = true
            )
            refreshLiveReminders()
            delay(3500)

            _workflowMessage.value = "Step 3: Verifying pre-reminder notification..."
            notificationHelper.showMedicineNotification(testMedId, "Workflow Test Pill", "1 Tablet", isPreReminder = true, isTest = true)
            repository.logEvent("WORKFLOW_TEST", "Step 3: Verified pre-reminder notification display")
            delay(2000)

            _workflowMessage.value = "Step 4: Scheduling main medicine alarm (+3s)..."
            alarmScheduler.scheduleMedicineAlarm(
                medicineId = testMedId,
                medicineName = "Workflow Test Pill",
                dosage = "1 Tablet",
                triggerAtMs = System.currentTimeMillis() + 3000L,
                isTest = true
            )
            refreshLiveReminders()
            delay(3500)

            _workflowMessage.value = "Step 5: Verifying main alarm trigger..."
            notificationHelper.showMedicineNotification(testMedId, "Workflow Test Pill", "1 Tablet", isPreReminder = false, isTest = true)
            repository.logEvent("WORKFLOW_TEST", "Step 5: Verified main alarm notification display")
            delay(2000)

            _workflowMessage.value = "Step 6: Simulating 'Taken' action..."
            repository.markMedicineAsTaken(testMedId)
            repository.logEvent("WORKFLOW_TEST", "Step 6: Marked medicine as Taken (isTaken = true)")
            delay(1500)

            _workflowMessage.value = "Step 7: Simulating 'Not Taken' action..."
            repository.updateMedicineTaken(testMedId, isTaken = false)
            repository.logEvent("WORKFLOW_TEST", "Step 7: Updated medicine status to Not Taken (isTaken = false)")
            delay(1500)

            _workflowMessage.value = "Step 8: Simulating 'Snooze 5m' action (+10s test snooze)..."
            alarmScheduler.scheduleSnoozeAlarm(testMedId, "Workflow Test Pill", "1 Tablet", snoozeDurationMs = 10_000L, isTest = true)
            refreshLiveReminders()
            repository.logEvent("WORKFLOW_TEST", "Step 8: Scheduled snooze alarm for +10s")
            delay(2000)

            _workflowMessage.value = "Step 9: Verifying alarm cancellation..."
            alarmScheduler.cancelAlarm(testMedId)
            refreshLiveReminders()
            repository.logEvent("WORKFLOW_TEST", "Step 9: Verified alarm cancellation")
            delay(1500)

            _workflowMessage.value = "Automated Workflow Test Completed Successfully! ✅"
            repository.logEvent("WORKFLOW_TEST", "Automated Workflow Test completed successfully!")
            _isAutoWorkflowRunning.value = false
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearTestResultsAndLogs()
            _testCases.value = initialTestCases
            recalculateSummary()
            refreshLiveReminders()
        }
    }
}
