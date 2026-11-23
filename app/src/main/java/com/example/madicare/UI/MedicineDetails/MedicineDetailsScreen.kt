package com.example.madicare.UI.MedicineDetails
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

// --- UI Colors ---
val PrimaryGreen = Color(0xFF1A9E56)
val LightGreenBg = Color(0xFFE8F7F1)
val TextDark = Color(0xFF1A1A1A)
val TextGray = Color(0xFF8A8A8A)
val LightGrayBg = Color(0xFFF5F6F8)
val BorderColor = Color(0xFFE5E7EB)

// ==========================================
// 1. STATEFUL SCREEN (Hooks up to ViewModel)
// ==========================================
@Composable
fun MedicineDetailsScreen(
    viewModel: MedicineDetailsViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onEditClick: (String) -> Unit = {},
    onDeleted: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showHistoryDialog by remember { mutableStateOf(false) }

    if (showHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            title = { Text("Medicine History & Doses Taken", fontWeight = FontWeight.Bold) },
            text = {
                if (uiState.historyLogs.isEmpty()) {
                    Text("No history recorded yet. Tap 'Mark as Taken' when you take your dose.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.historyLogs.forEach { log ->
                            Text(text = "• $log", fontSize = 14.sp, color = TextDark)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHistoryDialog = false }) {
                    Text("Close", color = PrimaryGreen)
                }
            }
        )
    }

    MedicineDetailsContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onDeleteClick = { viewModel.onDeleteClick(onDeleted) },
        onMarkTaken = viewModel::onMarkAsTakenClick,
        onEdit = { onEditClick(viewModel.medicineId) },
        onRefill = viewModel::onRefillClick,
        onHistory = { showHistoryDialog = true }
    )
}

// ==========================================
// 2. STATELESS UI CONTENT (Pure UI)
// ==========================================
@Composable
fun MedicineDetailsContent(
    uiState: MedicineDetailsUiState,
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMarkTaken: () -> Unit,
    onEdit: () -> Unit,
    onRefill: () -> Unit,
    onHistory: () -> Unit
) {
    Scaffold(
        containerColor = Color.White,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onBackClick() },
                    tint = TextDark
                )
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onDeleteClick() },
                    tint = TextDark
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Header (Bottle Image & Title) ---
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(Color(uiState.pillColorHex).copy(alpha = 0.25f))
                    .border(2.dp, Color(uiState.pillColorHex).copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = uiState.bottleDrawable),
                    contentDescription = "Medicine Illustration",
                    modifier = Modifier.size(90.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = uiState.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text(text = uiState.description, fontSize = 14.sp, color = TextGray)

            Spacer(modifier = Modifier.height(24.dp))

            // --- Refill Reminder / Low Stock Banner ---
            if (uiState.isLowStock) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Schedule, contentDescription = "Alert", tint = Color(0xFF856404))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Refill Reminder: Low stock! Only ${uiState.remaining} tablets left.",
                            color = Color(0xFF856404),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // --- Progress Card ---
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "${uiState.remaining}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                            Text(text = " / ${uiState.total}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextGray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "tablets remaining", fontSize = 12.sp, color = TextGray)
                        }
                        Text(text = uiState.percentageText, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Custom Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(LightGrayBg)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(uiState.progressFraction)
                                .clip(RoundedCornerShape(4.dp))
                                .background(PrimaryGreen)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- Dates Row ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DateColumn(title = "Start Date", value = uiState.startDate)
                DateColumn(title = "End Date", value = uiState.endDate)
                DateColumn(title = "Next Dose", value = uiState.nextDose, isNextDose = true)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Action Buttons ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ActionButton("Mark as Taken", Icons.Filled.CheckCircle, onMarkTaken)
                ActionButton("Edit", Icons.Outlined.Edit, onEdit)
                ActionButton("Refill", Icons.Outlined.Refresh, onRefill)
                ActionButton("History", Icons.Outlined.History, onHistory)
            }

            Spacer(modifier = Modifier.height(28.dp))

            // --- Dose Schedule ---
            Text(
                text = "Dose Schedule",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            uiState.schedule.forEach { dose ->
                DoseScheduleItem(dose)
                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==========================================
// 3. SUB-COMPONENTS
// ==========================================

@Composable
fun DateColumn(title: String, value: String, isNextDose: Boolean = false) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(text = title, fontSize = 11.sp, color = TextGray)
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isNextDose) TextDark else TextDark
        )
    }
}

@Composable
fun ActionButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(LightGreenBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = PrimaryGreen)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextDark, textAlign = TextAlign.Center)
    }
}

@Composable
fun DoseScheduleItem(dose: DoseItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(LightGrayBg)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Schedule,
            contentDescription = "Time",
            tint = PrimaryGreen,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = dose.time, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)

        Spacer(modifier = Modifier.weight(1f))

        Text(text = dose.amount, fontSize = 13.sp, color = TextDark)

        Spacer(modifier = Modifier.weight(1f))

        Text(text = dose.frequency, fontSize = 13.sp, color = TextGray)
    }
}

// ==========================================
// 4. CUSTOM BOTTLE DRAWING
// ==========================================
@Composable
fun LargePillBottleIcon() {
    val capColor = Color(0xFF006644)
    val bodyColor = Color(0xFFFFF3E0)

    Box(
        modifier = Modifier.size(width = 90.dp, height = 110.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(width = 50.dp, height = 20.dp)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(capColor)
        )
        Box(
            modifier = Modifier
                .padding(top = 24.dp)
                .size(width = 80.dp, height = 80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(bodyColor)
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy((-8).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                    PillShape(capColor)
                    PillShape(capColor)
                }
                Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                    PillShape(capColor)
                    PillShape(capColor)
                    PillShape(capColor)
                }
                Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                    PillShape(capColor)
                    PillShape(capColor)
                }
            }
        }
    }
}

@Composable
fun PillShape(color: Color) {
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.8f))
            .border(1.dp, Color.White, CircleShape)
    )
}

// ==========================================
// 5. PREVIEW
// ==========================================
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun PreviewMedicineDetailsScreen() {
    val dummyState = MedicineDetailsUiState(
        name = "Paracetamol",
        description = "500 mg - 1 tablet",
        remaining = 4,
        total = 30,
        startDate = "1 Oct 2026",
        endDate = "30 Oct 2026",
        nextDose = "Today, 8:00 PM",
        schedule = listOf(
            DoseItem("8:00 AM", "1 tablet", "Daily"),
            DoseItem("8:00 PM", "1 tablet", "Daily")
        ),
        historyLogs = listOf("Taken at 24 Oct 2026, 08:00 AM")
    )

    MaterialTheme {
        MedicineDetailsContent(
            uiState = dummyState,
            onBackClick = {},
            onDeleteClick = {},
            onMarkTaken = {},
            onEdit = {},
            onRefill = {},
            onHistory = {}
        )
    }
}
