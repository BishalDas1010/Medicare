package com.example.madicare.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.madicare.DATABASE.MedicareRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MedicineAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var alarmScheduler: MedicineAlarmScheduler

    @Inject
    lateinit var repository: MedicareRepository

    companion object {
        const val ACTION_MEDICINE_ALARM = "com.example.madicare.ACTION_MEDICINE_ALARM"
        const val ACTION_PRE_REMINDER = "com.example.madicare.ACTION_PRE_REMINDER"
        const val ACTION_TAKEN = "com.example.madicare.ACTION_TAKEN"
        const val ACTION_NOT_TAKEN = "com.example.madicare.ACTION_NOT_TAKEN"
        const val ACTION_SNOOZE = "com.example.madicare.ACTION_SNOOZE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val medicineId = intent.getLongExtra("MEDICINE_ID", 0L)
        val medicineName = intent.getStringExtra("MEDICINE_NAME") ?: "Medicine"
        val dosage = intent.getStringExtra("DOSAGE") ?: "1 Dose"
        val isTest = intent.getBooleanExtra("IS_TEST", false)

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (action) {
                    Intent.ACTION_BOOT_COMPLETED -> {
                        repository.logEvent("DEVICE_REBOOT_DETECTED", "Device reboot detected by receiver")
                        // Reschedule active alarms
                        MedicineAlarmScheduler.activeAlarms.values.filter { it.status == "SCHEDULED" }.forEach { alarmInfo ->
                            alarmScheduler.scheduleMedicineAlarm(
                                medicineId = alarmInfo.medicineId,
                                medicineName = alarmInfo.medicineName,
                                dosage = "1 Dose",
                                triggerAtMs = alarmInfo.scheduledTimeMs,
                                isTest = false
                            )
                        }
                        repository.logEvent("REMINDER_RESTORED_AFTER_REBOOT", "Restored active scheduled alarms after boot")
                    }

                    ACTION_PRE_REMINDER -> {
                        repository.logEvent("PRE_REMINDER_RECEIVED", "Pre-reminder triggered for $medicineName (ID: $medicineId)")
                        notificationHelper.showMedicineNotification(
                            medicineId = medicineId,
                            medicineName = medicineName,
                            dosage = dosage,
                            isPreReminder = true,
                            isTest = isTest
                        )
                        repository.logEvent("NOTIFICATION_DISPLAYED", "Pre-reminder notification displayed for $medicineName")
                    }

                    ACTION_MEDICINE_ALARM -> {
                        repository.logEvent("MAIN_ALARM_RECEIVED", "Main medicine alarm triggered for $medicineName (ID: $medicineId)")
                        notificationHelper.showMedicineNotification(
                            medicineId = medicineId,
                            medicineName = medicineName,
                            dosage = dosage,
                            isPreReminder = false,
                            isTest = isTest
                        )
                        repository.logEvent("NOTIFICATION_DISPLAYED", "Main medicine notification displayed for $medicineName")

                        // Launch Full-Screen Alarm Activity over lockscreen/apps
                        val alarmIntent = Intent(context, com.example.madicare.UI.alarm.AlarmActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra("MEDICINE_ID", medicineId)
                            putExtra("MEDICINE_NAME", medicineName)
                            putExtra("DOSAGE", dosage)
                        }
                        context.startActivity(alarmIntent)

                        MedicineAlarmScheduler.activeAlarms[medicineId]?.let {
                            MedicineAlarmScheduler.activeAlarms[medicineId] = it.copy(status = "TRIGGERED")
                        }
                    }

                    ACTION_TAKEN -> {
                        repository.logEvent("USER_SELECTED_TAKEN", "User tapped TAKEN for $medicineName (ID: $medicineId)")
                        notificationHelper.cancelNotification(medicineId)
                        repository.updateMedicineTaken(medicineId, isTaken = true)
                        repository.logEvent("DATABASE_STATUS_UPDATED", "Updated $medicineName status to TAKEN (isTaken=true)")
                    }

                    ACTION_NOT_TAKEN -> {
                        repository.logEvent("USER_SELECTED_NOT_TAKEN", "User tapped NOT TAKEN for $medicineName (ID: $medicineId)")
                        notificationHelper.cancelNotification(medicineId)
                        repository.updateMedicineTaken(medicineId, isTaken = false)
                        repository.logEvent("DATABASE_STATUS_UPDATED", "Updated $medicineName status to NOT TAKEN (isTaken=false)")
                    }

                    ACTION_SNOOZE -> {
                        repository.logEvent("USER_SELECTED_SNOOZE", "User tapped SNOOZE for $medicineName (ID: $medicineId)")
                        notificationHelper.cancelNotification(medicineId)

                        val snoozeDurationMs = if (isTest) 10_000L else 5 * 60 * 1000L // 10s for test, 5m for real
                        alarmScheduler.scheduleSnoozeAlarm(
                            medicineId = medicineId,
                            medicineName = medicineName,
                            dosage = dosage,
                            snoozeDurationMs = snoozeDurationMs,
                            isTest = isTest
                        )
                        repository.logEvent("SNOOZE_ALARM_SCHEDULED", "Snooze alarm scheduled for $medicineName in ${snoozeDurationMs / 1000}s")
                    }
                }
            } catch (e: Exception) {
                repository.logEvent("ALARM_RECEIVER_ERROR", "Error: ${e.localizedMessage}", isSuccess = false)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
