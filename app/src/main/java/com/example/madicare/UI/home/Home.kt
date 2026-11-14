package com.example.madicare.UI.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// FIXED: Changed from androidx.hilt.lifecycle to androidx.hilt.navigation
import androidx.hilt.navigation.compose.hiltViewModel

// YOUR NEW IMPORTS
import com.example.madicare.Data.DayModel
import com.example.madicare.Data.HomeUiState
import com.example.madicare.Data.Medicine
import com.example.madicare.Ennum.MedicineStatus
import java.util.Date

// --- Extracted Colors from Design ---
val PrimaryBlue = Color(0xFF1E65E7)
val LightBlueBg = Color(0xFFEFF6FF)
val PrimaryGreen = Color(0xFF10B981)
val LightGreenBg = Color(0xFFECFDF5)
val TextMain = Color(0xFF111827)
val TextSub = Color(0xFF6B7280)
val ScreenBg = Color(0xFFF8FAFC)
val CardBg = Color.White
val GrayUpcomingBg = Color(0xFFF3F4F6)
val GrayUpcomingIconBg = Color(0xFFE5E7EB)
val BorderColor = Color(0xFFE5E7EB)
val BlueBorder = Color(0xFF3B82F6)

// WHAT: The main entry point that connects Hilt to the UI
@Composable
fun MedicineAppScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onScheduleClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    HomeScreenContent(
        uiState = uiState,
        onMarkAsTaken = viewModel::markAsTaken,
        onScheduleClick = onScheduleClick,
        onDateSelected = viewModel::selectDate
    )
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onMarkAsTaken: (String) -> Unit = {},
    onScheduleClick: () -> Unit = {},
    onDateSelected: (Date) -> Unit = {}
) {
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize().background(ScreenBg),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PrimaryBlue)
        }
    } else {
        Box(modifier = Modifier.fillMaxSize().background(ScreenBg)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                TopBar()
                Spacer(modifier = Modifier.height(24.dp))

                DateHeader(dateString = uiState.dateString, isToday = uiState.isToday)
                Spacer(modifier = Modifier.height(16.dp))
                ProgressCard(
                    taken = uiState.medicinesTaken,
                    total = uiState.totalMedicines,
                    progressFraction = uiState.progressFraction,
                    nextTime = uiState.nextTime
                )

                Spacer(modifier = Modifier.height(24.dp))
                WeekCalendar(
                    weekDays = uiState.weekDays,
                    onDayClick = { day -> onDateSelected(day.date) }
                )

                Spacer(modifier = Modifier.height(28.dp))

                ScheduleHeader(total = uiState.totalMedicines)
                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.schedule.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Outlined.Medication, contentDescription = null, tint = TextSub, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No medicines scheduled yet", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextMain)
                            Text("Tap '+ Schedule' to add your first medicine.", fontSize = 13.sp, color = TextSub)
                        }
                    }
                } else {
                    uiState.schedule.forEach { medicine ->
                        ScheduleItem(medicine = medicine, onMarkAsTaken = onMarkAsTaken)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Extra space for FAB
                Spacer(modifier = Modifier.height(80.dp))
            }

            // Floating Action Button
            ExtendedFloatingActionButton(
                onClick = onScheduleClick,
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Schedule", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Schedule", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ==========================================
// SUB-COMPONENTS
// ==========================================

@Composable
fun TopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "App Icon", tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "Medicare", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextMain)
                Text(text = "Medication & Health Tracker", fontSize = 12.sp, color = TextSub)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(contentAlignment = Alignment.TopEnd) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = TextMain, modifier = Modifier.size(28.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                        .border(1.dp, ScreenBg, CircleShape)
                )
            }
            Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = TextMain, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
fun DateHeader(dateString: String, isToday: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = dateString, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextMain)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Outlined.CalendarToday, contentDescription = "Calendar", tint = PrimaryBlue, modifier = Modifier.size(20.dp))
        }

        if (isToday) {
            Surface(
                color = LightBlueBg,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Today",
                    color = PrimaryBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun WeekCalendar(
    weekDays: List<DayModel>,
    onDayClick: (DayModel) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        weekDays.forEach { day ->
            val bgColor = if (day.isSelected) PrimaryBlue else CardBg
            val textColor = if (day.isSelected) Color.White else TextMain
            val subTextColor = if (day.isSelected) Color.White.copy(alpha = 0.8f) else TextSub

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .clickable { onDayClick(day) }
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = day.dayName, fontSize = 12.sp, color = subTextColor)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = day.dayNumber, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
            }
        }
    }
}

@Composable
fun ProgressCard(taken: Int, total: Int, progressFraction: Float, nextTime: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Circular Progress
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(70.dp)) {
                Canvas(modifier = Modifier.size(70.dp)) {
                    val strokeWidth = 8.dp.toPx()
                    drawArc(
                        color = LightBlueBg,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                    drawArc(
                        color = PrimaryBlue,
                        startAngle = -90f,
                        sweepAngle = 360f * progressFraction,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Icon(Icons.Outlined.Medication, contentDescription = "Pill", tint = PrimaryBlue, modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text Info
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "$taken of $total", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextMain)
                Text(text = "Medications taken", fontSize = 14.sp, color = TextSub)
            }

            // Divider
            Box(modifier = Modifier.width(1.dp).height(50.dp).background(BorderColor))

            Spacer(modifier = Modifier.width(16.dp))

            // Next Time Info
            Column {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(LightBlueBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.CalendarToday, contentDescription = "Calendar", tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Next at", fontSize = 12.sp, color = TextSub)
                Text(text = nextTime, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextMain)
            }
        }
    }
}

@Composable
fun ScheduleHeader(total: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Today's Schedule", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextMain)
        Surface(color = LightBlueBg, shape = RoundedCornerShape(16.dp)) {
            Text(
                text = "$total medications",
                color = PrimaryBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun ScheduleItem(
    medicine: Medicine,
    onMarkAsTaken: (String) -> Unit = {}
) {
    val bgColor = when (medicine.status) {
        MedicineStatus.TAKEN -> LightGreenBg
        MedicineStatus.NEXT -> CardBg
        MedicineStatus.UPCOMING -> GrayUpcomingBg
    }
    val borderColor = if (medicine.status == MedicineStatus.NEXT) BlueBorder else Color.Transparent
    val iconTint = when (medicine.status) {
        MedicineStatus.TAKEN -> PrimaryGreen
        MedicineStatus.NEXT -> PrimaryBlue
        MedicineStatus.UPCOMING -> TextSub
    }
    val iconBg = when (medicine.status) {
        MedicineStatus.TAKEN -> PrimaryGreen.copy(alpha = 0.2f)
        MedicineStatus.NEXT -> LightBlueBg
        MedicineStatus.UPCOMING -> GrayUpcomingIconBg
    }
    val icon = when (medicine.status) {
        MedicineStatus.TAKEN -> Icons.Filled.Check
        MedicineStatus.NEXT -> Icons.Outlined.Notifications
        MedicineStatus.UPCOMING -> Icons.Outlined.Schedule
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(20.dp),
        border = if (medicine.status == MedicineStatus.NEXT) BorderStroke(1.5.dp, borderColor) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(24.dp).clip(CircleShape).background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = medicine.time, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextMain)
                }

                // Status Badge
                when (medicine.status) {
                    MedicineStatus.TAKEN -> {
                        Surface(color = Color.White.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp)) {
                            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Taken", color = PrimaryGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    MedicineStatus.NEXT -> {
                        Surface(color = LightBlueBg, shape = RoundedCornerShape(12.dp)) {
                            Text("Next", color = PrimaryBlue, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                        }
                    }
                    MedicineStatus.UPCOMING -> {
                        Text("Upcoming", color = TextSub, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(end = 8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Medication, contentDescription = "Pill", tint = iconTint, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = medicine.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextMain)
                        Text(text = medicine.dosage, fontSize = 13.sp, color = TextSub)
                    }
                }
                Icon(Icons.Default.MoreVert, contentDescription = "More", tint = TextSub)
            }

            // Action Button for NEXT status
            if (medicine.status == MedicineStatus.NEXT) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onMarkAsTaken(medicine.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mark as Taken", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun PreviewMedicineAppScreen() {
    MaterialTheme {
        HomeScreenContent(
            uiState = HomeUiState(
                dateString = "Sun, Mar 16, 2025",
                isToday = true,
                weekDays = listOf(
                    DayModel("Sun", "16", "Sun, Mar 16, 2025", Date(), true),
                    DayModel("Mon", "17", "Mon, Mar 17, 2025", Date(), false),
                    DayModel("Tue", "18", "Tue, Mar 18, 2025", Date(), false),
                    DayModel("Wed", "19", "Wed, Mar 19, 2025", Date(), false),
                    DayModel("Thu", "20", "Thu, Mar 20, 2025", Date(), false),
                    DayModel("Fri", "21", "Fri, Mar 21, 2025", Date(), false),
                    DayModel("Sat", "22", "Sat, Mar 22, 2025", Date(), false)
                ),
                schedule = listOf(
                    Medicine("1", "My Medication 01", "1 Tablet • Daily", "7:00 AM", MedicineStatus.TAKEN),
                    Medicine("2", "My Medication 01", "1 Tablet • Daily", "12:00 PM", MedicineStatus.NEXT),
                    Medicine("3", "My Medication 01", "1 Tablet • Daily", "6:00 PM", MedicineStatus.UPCOMING)
                )
            )
        )
    }
}
