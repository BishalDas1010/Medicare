package com.example.madicare.UI.alarm

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.madicare.DATABASE.MedicareRepository
import com.example.madicare.notification.MedicineAlarmScheduler
import com.example.madicare.notification.NotificationHelper
import com.example.madicare.ui.theme.MadicareTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import androidx.activity.enableEdgeToEdge

@AndroidEntryPoint
class AlarmActivity : ComponentActivity() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var alarmScheduler: MedicineAlarmScheduler

    @Inject
    lateinit var repository: MedicareRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        turnOnScreenAndShowOnLockscreen()

        val medicineId = intent.getLongExtra("MEDICINE_ID", 0L)
        val medicineName = intent.getStringExtra("MEDICINE_NAME") ?: "Paracetamol"
        val dosage = intent.getStringExtra("DOSAGE") ?: "500 mg • 1 tablet"
        val time = intent.getStringExtra("TIME") ?: "8:00 AM"

        // Play alarm sound
        notificationHelper.playAlarmSound()

        setContent {
            MadicareTheme {
                AlarmScreen(
                    medicineId = medicineId,
                    medicineName = medicineName,
                    dosage = dosage,
                    scheduledTime = time,
                    onMarkAsTaken = { id ->
                        notificationHelper.stopAlarmSound()
                        notificationHelper.cancelNotification(id)
                    },
                    onSnooze = { id ->
                        notificationHelper.stopAlarmSound()
                        notificationHelper.cancelNotification(id)
                        alarmScheduler.scheduleSnoozeAlarm(
                            medicineId = id,
                            medicineName = medicineName,
                            dosage = dosage,
                            snoozeDurationMs = 10 * 60 * 1000L // 10 minutes snooze
                        )
                    },
                    onDismiss = { id ->
                        notificationHelper.stopAlarmSound()
                        notificationHelper.cancelNotification(id)
                        finish()
                    }
                )
            }
        }
    }

    private fun turnOnScreenAndShowOnLockscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationHelper.stopAlarmSound()
    }
}
