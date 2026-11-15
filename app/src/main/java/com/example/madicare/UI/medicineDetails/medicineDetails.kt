package com.example.madicare.UI.medicineDetails

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.madicare.UI.medicineDetails.DoseScheduleItem
import com.example.madicare.UI.medicineDetails.MedicationUiEvent
import com.example.madicare.UI.medicineDetails.MedicationUiState
import com.example.madicare.UI.medicineDetails.MedicationViewModel

// --- Colors ---
val PrimaryGreen = Color(0xFF2E9E75)
val BackgroundGray = Color(0xFFF9F9FB)
val TextDark = Color(0xFF1E1E1E)
val TextGray = Color(0xFF8E8E93)

// --- Stateful Entry Point ---
@Composable
fun MedicationScreen(
    viewModel: MedicationViewModel = hiltViewModel(), // Injects ViewModel
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {}
) {
    // Collects state safely with lifecycle awareness
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showEditRefillDialog by remember { mutableStateOf(false) }
    var newRefillText by remember { mutableStateOf("") }

    if (showEditRefillDialog) {
        AlertDialog(
            onDismissRequest = { showEditRefillDialog = false },
            title = { Text("Edit Refill Reminder") },
            text = {
                OutlinedTextField(
                    value = newRefillText,
                    onValueChange = { newRefillText = it },
                    label = { Text("Tablets threshold") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    val count = newRefillText.toIntOrNull() ?: uiState.refillReminderCount
                    viewModel.updateRefillReminder(count)
                    showEditRefillDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditRefillDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    MedicationScreenContent(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                MedicationUiEvent.OnBackClick -> onBackClick()
                MedicationUiEvent.OnEditClick -> onEditClick()
                MedicationUiEvent.OnDeleteClick -> {
                    viewModel.handleEvent(event)
                    onBackClick()
                }
                MedicationUiEvent.OnEditRefillReminderClick -> {
                    newRefillText = uiState.refillReminderCount.toString()
                    showEditRefillDialog = true
                }
                else -> viewModel.handleEvent(event)
            }
        }
    )
}

// --- Stateless UI ---
@Composable
fun MedicationScreenContent(
    uiState: MedicationUiState,
    onEvent: (MedicationUiEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBar(onEvent)

        Spacer(modifier = Modifier.height(24.dp))

        MedicationHeader(uiState)

        Spacer(modifier = Modifier.height(16.dp))

        ProgressCard(uiState)

        Spacer(modifier = Modifier.height(20.dp))

        DatesRow(uiState)

        Spacer(modifier = Modifier.height(24.dp))

        ActionButtonsRow(onEvent)

        Spacer(modifier = Modifier.height(24.dp))

        DoseScheduleSection(uiState.doseSchedule)

        Spacer(modifier = Modifier.height(24.dp))

        RefillReminderSection(uiState.refillReminderCount, onEvent)

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun TopBar(onEvent: (MedicationUiEvent) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onEvent(MedicationUiEvent.OnBackClick) }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextDark)
        }
        Row {
            IconButton(onClick = { onEvent(MedicationUiEvent.OnEditClick) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextDark)
            }
            IconButton(onClick = { onEvent(MedicationUiEvent.OnDeleteClick) }) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = TextDark)
            }
        }
    }
}

@Composable
fun MedicationHeader(uiState: MedicationUiState) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(Color.White, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = uiState.bottleDrawable),
            contentDescription = "Pill Bottle",
            modifier = Modifier.size(70.dp)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(text = uiState.medicationName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextDark)
    Text(
        text = uiState.dosageInfo,
        fontSize = 14.sp,
        color = TextGray,
        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
    )
}

@Composable
fun ProgressCard(uiState: MedicationUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${uiState.remainingTablets} / ${uiState.totalTablets}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreen
                )
                Text(
                    text = " tablets remaining",
                    fontSize = 14.sp,
                    color = TextGray,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { uiState.progress },
                    modifier = Modifier.weight(1f).height(8.dp),
                    color = PrimaryGreen,
                    trackColor = BackgroundGray,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Text(
                    text = uiState.progressPercentageText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }
    }
}

@Composable
fun DatesRow(uiState: MedicationUiState) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        DateItem(title = "Start Date", value = uiState.startDate, modifier = Modifier.weight(1f))
        DateItem(title = "End Date", value = uiState.endDate, modifier = Modifier.weight(1f))
        DateItem(title = "Next Dose", value = uiState.nextDoseTime, modifier = Modifier.weight(1f))
    }
}

@Composable
fun DateItem(title: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = title, fontSize = 12.sp, color = TextGray)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextDark, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun ActionButtonsRow(onEvent: (MedicationUiEvent) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ActionButton(
            icon = Icons.Default.CheckCircle,
            label = "Mark as Taken",
            modifier = Modifier.weight(1f),
            onClick = { onEvent(MedicationUiEvent.OnMarkAsTakenClick) }
        )
        ActionButton(
            icon = Icons.Outlined.AddBox,
            label = "Add Stock",
            modifier = Modifier.weight(1f),
            onClick = { onEvent(MedicationUiEvent.OnAddStockClick) }
        )
        ActionButton(
            icon = Icons.Outlined.History,
            label = "View History",
            modifier = Modifier.weight(1f),
            onClick = { onEvent(MedicationUiEvent.OnViewHistoryClick) }
        )
    }
}

@Composable
fun ActionButton(icon: ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick // Makes the card clickable
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = PrimaryGreen, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = label, fontSize = 11.sp, color = TextDark, textAlign = TextAlign.Center, lineHeight = 14.sp)
        }
    }
}

@Composable
fun DoseScheduleSection(schedule: List<DoseScheduleItem>) {
    Text(
        text = "Dose Schedule",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = TextDark,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            schedule.forEachIndexed { index, item ->
                DoseScheduleItemRow(item)
                if (index != schedule.lastIndex) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun DoseScheduleItemRow(item: DoseScheduleItem) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.Schedule, contentDescription = "Time", tint = TextDark, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = item.time, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextDark, modifier = Modifier.weight(1f))
        Text(text = item.dose, fontSize = 14.sp, color = TextGray, modifier = Modifier.weight(1f))
        Text(text = item.frequency, fontSize = 14.sp, color = TextGray, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
    }
}

@Composable
fun RefillReminderSection(threshold: Int, onEvent: (MedicationUiEvent) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "Refill Reminder", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
        IconButton(onClick = { onEvent(MedicationUiEvent.OnEditRefillReminderClick) }) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Reminder", tint = TextDark, modifier = Modifier.size(20.dp))
        }
    }
    Text(
        text = "Notify when $threshold tablets left",
        fontSize = 13.sp,
        color = TextGray,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
    )
}

// --- Preview ---
@Preview(showBackground = true, backgroundColor = 0xFFF9F9FB)
@Composable
fun PreviewMedicationTracker() {
    MaterialTheme {
        // Providing dummy state for the preview
        MedicationScreenContent(
            uiState = MedicationUiState(
                medicationName = "Paracetamol",
                dosageInfo = "500 mg • 1 tablet daily",
                remainingTablets = 20,
                totalTablets = 30,
                startDate = "1 Oct 2026",
                endDate = "30 Oct 2026",
                nextDoseTime = "Today, 8:00 PM",
                doseSchedule = listOf(
                    DoseScheduleItem("8:00 AM", "1 tablet", "Daily"),
                    DoseScheduleItem("8:00 PM", "1 tablet", "Daily")
                ),
                refillReminderCount = 5
            ),
            onEvent = {} // Do nothing in preview
        )
    }
}
