package com.example.madicare.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class ScheduledAlarmInfo(
    val medicineId: Long,
    val medicineName: String,
    val scheduledTimeMs: Long,
    val preReminderTimeMs: Long?,
    val alarmType: String, // "MAIN", "PRE_REMINDER", "SNOOZE"
    val status: String,    // "SCHEDULED", "TRIGGERED", "SNOOZED", "CANCELLED"
    val snoozeCount: Int = 0,
    val pendingIntentId: Int
)

@Singleton
class MedicineAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        const val ACTION_MEDICINE_ALARM = "com.example.madicare.ACTION_MEDICINE_ALARM"
        const val ACTION_PRE_REMINDER = "com.example.madicare.ACTION_PRE_REMINDER"

        val activeAlarms = mutableMapOf<Long, ScheduledAlarmInfo>()
    }

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun scheduleMedicineAlarm(
        medicineId: Long,
        medicineName: String,
        dosage: String,
        triggerAtMs: Long,
        preReminderMs: Long? = null,
        isTest: Boolean = false
    ) {
        // 1. Schedule Pre-reminder if provided
        preReminderMs?.let { preTime ->
            if (preTime > System.currentTimeMillis()) {
                val preIntent = Intent(context, MedicineAlarmReceiver::class.java).apply {
                    action = ACTION_PRE_REMINDER
                    putExtra("MEDICINE_ID", medicineId)
                    putExtra("MEDICINE_NAME", medicineName)
                    putExtra("DOSAGE", dosage)
                    putExtra("IS_TEST", isTest)
                }
                val prePendingIntent = PendingIntent.getBroadcast(
                    context,
                    (medicineId * 100 + 1).toInt(),
                    preIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                scheduleExact(preTime, prePendingIntent)
            }
        }

        // 2. Schedule Main Alarm
        val mainIntent = Intent(context, MedicineAlarmReceiver::class.java).apply {
            action = ACTION_MEDICINE_ALARM
            putExtra("MEDICINE_ID", medicineId)
            putExtra("MEDICINE_NAME", medicineName)
            putExtra("DOSAGE", dosage)
            putExtra("IS_TEST", isTest)
        }
        val mainPendingIntent = PendingIntent.getBroadcast(
            context,
            medicineId.toInt(),
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExact(triggerAtMs, mainPendingIntent)

        // Track active alarm
        activeAlarms[medicineId] = ScheduledAlarmInfo(
            medicineId = medicineId,
            medicineName = medicineName,
            scheduledTimeMs = triggerAtMs,
            preReminderTimeMs = preReminderMs,
            alarmType = if (isTest) "TEST_MAIN" else "MAIN",
            status = "SCHEDULED",
            snoozeCount = 0,
            pendingIntentId = medicineId.toInt()
        )
    }

    fun scheduleSnoozeAlarm(
        medicineId: Long,
        medicineName: String,
        dosage: String,
        snoozeDurationMs: Long = 5 * 60 * 1000L, // Default 5 mins
        isTest: Boolean = false
    ) {
        val triggerAtMs = System.currentTimeMillis() + snoozeDurationMs
        val snoozeIntent = Intent(context, MedicineAlarmReceiver::class.java).apply {
            action = ACTION_MEDICINE_ALARM
            putExtra("MEDICINE_ID", medicineId)
            putExtra("MEDICINE_NAME", medicineName)
            putExtra("DOSAGE", dosage)
            putExtra("IS_SNOOZE", true)
            putExtra("IS_TEST", isTest)
        }

        val pendingIntentId = (medicineId * 100 + 99).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            pendingIntentId,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExact(triggerAtMs, pendingIntent)

        val existing = activeAlarms[medicineId]
        val currentSnoozeCount = (existing?.snoozeCount ?: 0) + 1

        activeAlarms[medicineId] = ScheduledAlarmInfo(
            medicineId = medicineId,
            medicineName = medicineName,
            scheduledTimeMs = triggerAtMs,
            preReminderTimeMs = null,
            alarmType = if (isTest) "TEST_SNOOZE" else "SNOOZE",
            status = "SNOOZED",
            snoozeCount = currentSnoozeCount,
            pendingIntentId = pendingIntentId
        )
    }

    private fun scheduleExact(triggerAtMs: Long, pendingIntent: PendingIntent) {
        if (canScheduleExactAlarms()) {
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMs, pendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
        }
    }

    fun cancelAlarm(medicineId: Long) {
        val mainIntent = Intent(context, MedicineAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicineId.toInt(),
            mainIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }

        activeAlarms[medicineId]?.let {
            activeAlarms[medicineId] = it.copy(status = "CANCELLED")
        }
    }

    fun cancelAllAlarms() {
        activeAlarms.keys.toList().forEach { id ->
            cancelAlarm(id)
        }
        activeAlarms.clear()
    }
}
