package com.example.madicare.UI.alarm

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.madicare.R

enum class AlarmUIState {
    ACTIVE_ALARM,
    TAKEN_SUCCESS,
    NOT_CONFIRMED
}

private val PrimaryGreen = Color(0xFF00875A)
private val TextDark = Color(0xFF1F2937)
private val TextGray = Color(0xFF6B7280)
private val LightGrayBg = Color(0xFFF3F4F6)
private val LightPinkBg = Color(0xFFFDF2F2)
private val DangerRed = Color(0xFFE53935)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AlarmScreen(
    medicineId: Long,
    medicineName: String,
    dosage: String,
    scheduledTime: String,
    onMarkAsTaken: (Long) -> Unit,
    onSnooze: (Long) -> Unit,
    onDismiss: (Long) -> Unit
) {
    var uiState by remember { mutableStateOf(AlarmUIState.ACTIVE_ALARM) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF4B5563)) // Top dark gray background
    ) {
        // --- Top Header Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD1D5DB)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🧑‍⚕️", fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Good Morning,",
                        fontSize = 12.sp,
                        color = Color(0xFFD1D5DB)
                    )
                    Text(
                        text = "Bishal 👋",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0x33FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notification",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF6B00))
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                )
            }
        }

        // --- Main White Bottom Card ---
        Card(
            modifier = Modifier
                .fillMaxSize(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = { fadeIn() with fadeOut() }
            ) { state ->
                when (state) {
                    AlarmUIState.ACTIVE_ALARM -> ActiveAlarmContent(
                        medicineName = medicineName,
                        dosage = dosage,
                        onTakenClick = {
                            uiState = AlarmUIState.TAKEN_SUCCESS
                            onMarkAsTaken(medicineId)
                        },
                        onSnoozeClick = {
                            onSnooze(medicineId)
                            onDismiss(medicineId)
                        },
                        onSkipClick = {
                            uiState = AlarmUIState.NOT_CONFIRMED
                        }
                    )

                    AlarmUIState.TAKEN_SUCCESS -> TakenSuccessContent(
                        medicineName = medicineName,
                        dosage = dosage,
                        onDoneClick = {
                            onDismiss(medicineId)
                        }
                    )

                    AlarmUIState.NOT_CONFIRMED -> NotConfirmedContent(
                        scheduledTime = scheduledTime,
                        medicineName = medicineName,
                        dosage = dosage,
                        onTakenClick = {
                            uiState = AlarmUIState.TAKEN_SUCCESS
                            onMarkAsTaken(medicineId)
                        },
                        onSnoozeClick = {
                            onSnooze(medicineId)
                            onDismiss(medicineId)
                        },
                        onTakeLaterClick = {
                            onDismiss(medicineId)
                        }
                    )
                }
            }
        }
    }
}

// ==========================================
// SCREEN 1: ACTIVE ALARM CONTENT
// ==========================================
@Composable
fun ActiveAlarmContent(
    medicineName: String,
    dosage: String,
    onTakenClick: () -> Unit,
    onSnoozeClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Center Graphic: Pill Bottle surrounded by soft circular background glow
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(Color(0xFFF3F4F6)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.bluebotttle),
                contentDescription = "Medicine Bottle",
                modifier = Modifier.size(120.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Text(
                text = "Time to take your\nmedicine",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                textAlign = TextAlign.Center,
                lineHeight = 30.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = medicineName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = dosage,
                fontSize = 14.sp,
                color = TextGray
            )
        }

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onTakenClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text("Mark as Taken", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }

            Button(
                onClick = onSnoozeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightGrayBg)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Alarm,
                        contentDescription = null,
                        tint = TextDark,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Snooze 10 minutes", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                }
            }

            Button(
                onClick = onSkipClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightPinkBg)
            ) {
                Text("Skip", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DangerRed)
            }
        }
    }
}

// ==========================================
// SCREEN 2: SUCCESS CONFIRMATION CONTENT
// ==========================================
@Composable
fun TakenSuccessContent(
    medicineName: String,
    dosage: String,
    onDoneClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Center Graphic: Soft green glow circle + Checkmark circle
        Box(
            modifier = Modifier
                .size(170.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F5E9)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00A86B)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Success",
                    tint = Color.White,
                    modifier = Modifier.size(54.dp)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Text(
                text = "Great! 🎉",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "You have taken your medicine.",
                fontSize = 14.sp,
                color = TextGray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Medicine Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE5E7EB)))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.bluebotttle),
                        contentDescription = null,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(medicineName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                        Text(dosage, fontSize = 13.sp, color = TextGray)
                    }
                }
            }
        }

        Button(
            onClick = onDoneClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
        ) {
            Text("Done", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

// ==========================================
// SCREEN 3: DOSE NOT CONFIRMED CONTENT
// ==========================================
@Composable
fun NotConfirmedContent(
    scheduledTime: String,
    medicineName: String,
    dosage: String,
    onTakenClick: () -> Unit,
    onSnoozeClick: () -> Unit,
    onTakeLaterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Center Graphic: Soft Red Glow Circle + Notification Bell
        Box(
            modifier = Modifier
                .size(170.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFEBEE)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFE0E0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Alert",
                    tint = DangerRed,
                    modifier = Modifier.size(50.dp)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Text(
                text = "Dose not confirmed",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your $scheduledTime medicine has not\nbeen confirmed yet.",
                fontSize = 14.sp,
                color = TextGray,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onTakenClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text("Mark as Taken", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }

            Button(
                onClick = onSnoozeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightGrayBg)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Alarm,
                        contentDescription = null,
                        tint = TextDark,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Snooze 10 minutes", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                }
            }

            TextButton(
                onClick = onTakeLaterClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("I will take later", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DangerRed)
            }
        }
    }
}
