package com.example.madicare.UI.alarm

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.madicare.R

import com.example.madicare.ui.theme.*

enum class AlarmUIState {
    ACTIVE_ALARM,
    TAKEN_SUCCESS,
    NOT_CONFIRMED
}

// ---- Glass Effect Overlays ----
private val GlassFill = Color.White.copy(alpha = 0.16f)
private val GlassBorder = Color.White.copy(alpha = 0.30f)

// ==========================================
// ROOT SCREEN
// ==========================================
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

    val topColor by animateColorAsState(
        targetValue = when (uiState) {
            AlarmUIState.ACTIVE_ALARM -> ActiveTop
            AlarmUIState.TAKEN_SUCCESS -> SuccessTop
            AlarmUIState.NOT_CONFIRMED -> AlertTop
        },
        animationSpec = tween(600),
        label = "topColor"
    )
    val bottomColor by animateColorAsState(
        targetValue = when (uiState) {
            AlarmUIState.ACTIVE_ALARM -> ActiveBottom
            AlarmUIState.TAKEN_SUCCESS -> SuccessBottom
            AlarmUIState.NOT_CONFIRMED -> AlertBottom
        },
        animationSpec = tween(600),
        label = "bottomColor"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(topColor, bottomColor)))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Scheduled time pill
        Row(
            modifier = Modifier
                .padding(top = 20.dp)
                .align(Alignment.CenterHorizontally)
                .clip(CircleShape)
                .background(GlassFill)
                .border(1.dp, GlassBorder, CircleShape)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Scheduled · $scheduledTime",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        AnimatedContent(
            targetState = uiState,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(250)) },
            label = "alarmState"
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
                    onSkipClick = { uiState = AlarmUIState.NOT_CONFIRMED }
                )

                AlarmUIState.TAKEN_SUCCESS -> TakenSuccessContent(
                    medicineName = medicineName,
                    dosage = dosage,
                    onDoneClick = { onDismiss(medicineId) }
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
                    onTakeLaterClick = { onDismiss(medicineId) }
                )
            }
        }
    }
}

// ==========================================
// SCREEN 1: ACTIVE ALARM
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
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        PulsingRings(ringColor = Color.White) {
            Box(
                modifier = Modifier
                    .size(132.dp)
                    .shadow(16.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bluebotttle),
                    contentDescription = "Medicine Bottle",
                    modifier = Modifier.size(84.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Time to take your\nmedicine",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = 34.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        MedicineGlassCard(medicineName = medicineName, dosage = dosage)

        Spacer(modifier = Modifier.weight(1f))

        SolidActionButton(
            text = "Mark as Taken",
            textColor = PrimaryGreen,
            icon = Icons.Default.Check,
            onClick = onTakenClick
        )
        Spacer(modifier = Modifier.height(12.dp))
        GlassActionButton(
            text = "Snooze 10 minutes",
            icon = Icons.Outlined.Alarm,
            onClick = onSnoozeClick
        )
        TextButton(
            onClick = onSkipClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Skip this dose",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

// ==========================================
// SCREEN 2: SUCCESS
// ==========================================
@Composable
fun TakenSuccessContent(
    medicineName: String,
    dosage: String,
    onDoneClick: () -> Unit
) {
    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(220.dp)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.14f))
            )
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f))
            )
            Box(
                modifier = Modifier
                    .size(116.dp)
                    .shadow(16.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Success",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(64.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Great! 🎉",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "You have taken your medicine.",
            fontSize = 15.sp,
            color = Color.White.copy(alpha = 0.85f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        MedicineGlassCard(medicineName = medicineName, dosage = dosage)

        Spacer(modifier = Modifier.weight(1f))

        SolidActionButton(
            text = "Done",
            textColor = PrimaryGreen,
            icon = null,
            onClick = onDoneClick
        )
    }
}

// ==========================================
// SCREEN 3: DOSE NOT CONFIRMED
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
    val transition = rememberInfiniteTransition(label = "bell")
    val swing = transition.animateFloat(
        initialValue = -14f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "swing"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        PulsingRings(ringColor = Color.White) {
            Box(
                modifier = Modifier
                    .size(124.dp)
                    .shadow(16.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Alert",
                    tint = DangerRed,
                    modifier = Modifier
                        .size(60.dp)
                        .graphicsLayer { rotationZ = swing.value }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Dose not confirmed",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your $scheduledTime medicine has not\nbeen confirmed yet.",
            fontSize = 15.sp,
            color = Color.White.copy(alpha = 0.88f),
            textAlign = TextAlign.Center,
            lineHeight = 21.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        MedicineGlassCard(medicineName = medicineName, dosage = dosage)

        Spacer(modifier = Modifier.weight(1f))

        SolidActionButton(
            text = "Mark as Taken",
            textColor = PrimaryGreen,
            icon = Icons.Default.Check,
            onClick = onTakenClick
        )
        Spacer(modifier = Modifier.height(12.dp))
        GlassActionButton(
            text = "Snooze 10 minutes",
            icon = Icons.Outlined.Alarm,
            onClick = onSnoozeClick
        )
        TextButton(
            onClick = onTakeLaterClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "I will take later",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

// ==========================================
// SHARED COMPONENTS
// ==========================================

/** Two expanding, fading rings behind [content]. */
@Composable
private fun PulsingRings(
    ringColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "rings")
    val progress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringProgress"
    )

    Box(
        modifier = modifier.size(280.dp),
        contentAlignment = Alignment.Center
    ) {
        repeat(2) { index ->
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .graphicsLayer {
                        val p = (progress.value + index * 0.5f) % 1f
                        val s = 1f + p * 0.85f
                        scaleX = s
                        scaleY = s
                        alpha = (1f - p) * 0.35f
                    }
                    .background(ringColor, CircleShape)
            )
        }
        Box(
            modifier = Modifier
                .size(176.dp)
                .background(Color.White.copy(alpha = 0.16f), CircleShape)
        )
        content()
    }
}

/** Frosted card showing the medicine name and dosage. */
@Composable
private fun MedicineGlassCard(medicineName: String, dosage: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GlassFill)
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.bluebotttle),
                contentDescription = null,
                modifier = Modifier.size(34.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = medicineName,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = dosage,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

/** High-contrast primary button (white with colored text). */
@Composable
private fun SolidActionButton(
    text: String,
    textColor: Color,
    icon: ImageVector?,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(29.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = textColor
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

/** Translucent secondary button with a thin white outline. */
@Composable
private fun GlassActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(27.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = GlassFill,
            contentColor = Color.White
        ),
        border = BorderStroke(1.dp, GlassBorder),
        elevation = null
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

// ==========================================
// PREVIEWS
// ==========================================

@Composable
private fun PreviewBackdrop(top: Color, bottom: Color, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(top, bottom)))
    ) { content() }
}

@Preview(showBackground = true, showSystemUi = true, name = "Alarm Screen (interactive)")
@Composable
private fun AlarmScreenPreview() {
    AlarmScreen(
        medicineId = 1L,
        medicineName = "Paracetamol 500mg",
        dosage = "1 tablet",
        scheduledTime = "08:00 AM",
        onMarkAsTaken = {},
        onSnooze = {},
        onDismiss = {}
    )
}

@Preview(showBackground = true, showSystemUi = true, name = "1 - Active Alarm")
@Composable
private fun ActiveAlarmContentPreview() {
    PreviewBackdrop(ActiveTop, ActiveBottom) {
        ActiveAlarmContent(
            medicineName = "Paracetamol 500mg",
            dosage = "1 tablet",
            onTakenClick = {},
            onSnoozeClick = {},
            onSkipClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "2 - Taken Success")
@Composable
private fun TakenSuccessContentPreview() {
    PreviewBackdrop(SuccessTop, SuccessBottom) {
        TakenSuccessContent(
            medicineName = "Paracetamol 500mg",
            dosage = "1 tablet",
            onDoneClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "3 - Not Confirmed")
@Composable
private fun NotConfirmedContentPreview() {
    PreviewBackdrop(AlertTop, AlertBottom) {
        NotConfirmedContent(
            scheduledTime = "08:00 AM",
            medicineName = "Paracetamol 500mg",
            dosage = "1 tablet",
            onTakenClick = {},
            onSnoozeClick = {},
            onTakeLaterClick = {}
        )
    }
}