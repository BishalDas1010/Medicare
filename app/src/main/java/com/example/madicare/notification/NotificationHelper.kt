package com.example.madicare.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.madicare.MainActivity
import com.example.madicare.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var ringtone: Ringtone? = null

    companion object {
        const val CHANNEL_ID = "medicine_reminder_alarm_channel"
        const val CHANNEL_NAME = "Medicine Alarms & Reminders"

        const val ACTION_TAKEN = "com.example.madicare.ACTION_TAKEN"
        const val ACTION_NOT_TAKEN = "com.example.madicare.ACTION_NOT_TAKEN"
        const val ACTION_SNOOZE = "com.example.madicare.ACTION_SNOOZE"
    }

    init {
        createNotificationChannel()
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Loud alarm sounds and full-screen reminders for taking medicines"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
                setSound(alarmUri, audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun playAlarmSound() {
        try {
            stopAlarmSound()
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ringtone = RingtoneManager.getRingtone(context, alarmUri).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    isLooping = true
                }
                play()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopAlarmSound() {
        try {
            ringtone?.let {
                if (it.isPlaying) {
                    it.stop()
                }
            }
            ringtone = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showMedicineNotification(
        medicineId: Long,
        medicineName: String,
        dosage: String,
        isPreReminder: Boolean = false,
        isTest: Boolean = false
    ) {
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        val title = if (isPreReminder) {
            "⏰ Upcoming Medicine: $medicineName"
        } else {
            "🚨 MEDICINE ALARM: $medicineName"
        }

        val body = if (isPreReminder) {
            "Pre-Reminder: $dosage scheduled shortly"
        } else {
            "Dose: $dosage. Confirm Taken, Not Taken, or Snooze to turn off alarm."
        }

        val contentIntent = Intent(context, com.example.madicare.UI.alarm.AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("MEDICINE_ID", medicineId)
            putExtra("MEDICINE_NAME", medicineName)
            putExtra("DOSAGE", dosage)
        }
        val pendingContentIntent = PendingIntent.getActivity(
            context,
            medicineId.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val takenIntent = Intent(context, MedicineAlarmReceiver::class.java).apply {
            action = ACTION_TAKEN
            putExtra("MEDICINE_ID", medicineId)
            putExtra("MEDICINE_NAME", medicineName)
            putExtra("IS_TEST", isTest)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            (medicineId * 10 + 1).toInt(),
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notTakenIntent = Intent(context, MedicineAlarmReceiver::class.java).apply {
            action = ACTION_NOT_TAKEN
            putExtra("MEDICINE_ID", medicineId)
            putExtra("MEDICINE_NAME", medicineName)
            putExtra("IS_TEST", isTest)
        }
        val notTakenPendingIntent = PendingIntent.getBroadcast(
            context,
            (medicineId * 10 + 2).toInt(),
            notTakenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, MedicineAlarmReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra("MEDICINE_ID", medicineId)
            putExtra("MEDICINE_NAME", medicineName)
            putExtra("DOSAGE", dosage)
            putExtra("IS_TEST", isTest)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (medicineId * 10 + 3).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.bluebotttle)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(alarmUri)
            .setVibrate(longArrayOf(0, 1000, 500, 1000, 500, 1000))
            .setOngoing(!isPreReminder)
            .setAutoCancel(false)
            .setContentIntent(pendingContentIntent)
            .setFullScreenIntent(pendingContentIntent, true)

        if (!isPreReminder) {
            builder.addAction(R.drawable.bluebotttle, "Taken ✅", takenPendingIntent)
            builder.addAction(R.drawable.bluebotttle, "Not Taken ❌", notTakenPendingIntent)
            builder.addAction(R.drawable.bluebotttle, "Snooze 5m 💤", snoozePendingIntent)

            // Start playing the loopable alarm ringtone!
            playAlarmSound()
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(medicineId.toInt(), builder.build())
    }

    fun cancelNotification(medicineId: Long) {
        stopAlarmSound()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(medicineId.toInt())
    }
}
