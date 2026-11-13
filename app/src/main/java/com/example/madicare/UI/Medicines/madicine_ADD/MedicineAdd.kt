package com.example.madicare.UI.Medicines

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.madicare.R
import com.example.madicare.UI.PrimaryGreen
import com.example.madicare.UI.TextGray

// --- UI Colors ---
val FieldBgColor = Color(0xFFF5F6F8)


// Mock list of drawable resources (Replace with your actual R.drawable IDs)
val bottleOptions = listOf(
    R.drawable.buttle,
    R.drawable.buttle, // e.g., R.drawable.buttle_orange
    R.drawable.buttle  // e.g., R.drawable.buttle_red
)

// ==========================================
// 1. STATEFUL SCREEN (Hooks up to ViewModel)
// ==========================================
@Composable
fun AddMedicineScreen(
    viewModel: AddMedicineViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showTimePicker by remember { mutableStateOf(false) }

    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { time ->
                viewModel.addTime(time)
                showTimePicker = false
            }
        )
    }

    AddMedicineContent(
        uiState = uiState,
        onNameChange = viewModel::onNameChange,
        onStrengthChange = viewModel::onStrengthChange,
        onTotalTabletsChange = viewModel::onTotalTabletsChange,
        onDoseChange = viewModel::onDoseChange,
        onBottleSelected = { viewModel.onBottleSelected(it) }, // Pass the new event
        onAddTimeClick = { showTimePicker = true },
        onSaveClick = { viewModel.onSaveClicked { onBackClick() } },
        onBackClick = onBackClick
    )
}

// ==========================================
// 2. STATELESS UI CONTENT (Pure UI)
// ==========================================
@Composable
fun AddMedicineContent(
    uiState: AddMedicineUiState,
    onNameChange: (String) -> Unit,
    onStrengthChange: (String) -> Unit,
    onTotalTabletsChange: (String) -> Unit,
    onDoseChange: (String) -> Unit,
    onBottleSelected: (Int) -> Unit, // New parameter
    onAddTimeClick: () -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        containerColor = Color.White,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.clickable { onBackClick() }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Add Medicine",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = onSaveClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(25.dp),
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Save Medicine", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // --- Medicine Details Section ---
            SectionTitle("Medicine Details")

            // NEW: Bottle Selection Row
            Text(text = "Select Bottle Icon", fontSize = 12.sp, color = TextGray, modifier = Modifier.padding(bottom = 8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                items(bottleOptions) { bottleId ->
                    BottleOptionItem(
                        bottleId = bottleId,
                        isSelected = uiState.selectedBottleId == bottleId,
                        onClick = { onBottleSelected(bottleId) }
                    )
                }
            }

            CustomTextField(
                label = "Medicine Name",
                value = uiState.medicineName,
                onValueChange = onNameChange
            )
            Spacer(modifier = Modifier.height(12.dp))

            CustomTextField(
                label = "Strength (optional)",
                value = uiState.strength,
                onValueChange = onStrengthChange
            )
            Spacer(modifier = Modifier.height(12.dp))

            CustomTextField(
                label = "Total Tablets in Pack",
                value = uiState.totalTablets,
                onValueChange = onTotalTabletsChange
            )
            Spacer(modifier = Modifier.height(12.dp))

            CustomTextField(
                label = "Dose per time",
                value = uiState.dosePerTime,
                onValueChange = onDoseChange
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Schedule Section ---
            SectionTitle("Schedule")

            Text(text = "How often?", fontSize = 12.sp, color = TextGray, modifier = Modifier.padding(bottom = 4.dp))
            val FieldBgColor = null
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = uiState.frequency, color = TextDark, fontSize = 14.sp)
                    Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Dropdown", tint = TextGray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Time", fontSize = 12.sp, color = TextGray, modifier = Modifier.padding(bottom = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.times.forEach { time ->
                    TimeChip(text = time)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, BorderColor, RoundedCornerShape(22.dp))
                    .clickable { onAddTimeClick() },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Time", modifier = Modifier.size(16.dp), tint = TextDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Add Time", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ==========================================
// 3. SUB-COMPONENTS
// ==========================================

// NEW: Bottle Item Composable
@Composable
fun BottleOptionItem(
    bottleId: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) PrimaryGreen.copy(alpha = 0.1f) else FieldBgColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) PrimaryGreen else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = bottleId),
            contentDescription = "Bottle Option",
            tint = Color.Unspecified, // Preserves original drawable colors
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = TextDark,
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

@Composable
fun CustomTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column {
        Text(text = label, fontSize = 12.sp, color = TextGray, modifier = Modifier.padding(bottom = 4.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = FieldBgColor,
                unfocusedContainerColor = FieldBgColor,
                disabledContainerColor = FieldBgColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, color = TextDark),
            singleLine = true
        )
    }
}

@Composable
fun TimeChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(FieldBgColor)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = text, fontSize = 12.sp, color = TextDark, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val currentTime = java.util.Calendar.getInstance()
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(java.util.Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(java.util.Calendar.MINUTE),
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val hour = timePickerState.hour
                    val minute = timePickerState.minute
                    val amPm = if (hour >= 12) "PM" else "AM"
                    val hour12 = when {
                        hour == 0 -> 12
                        hour > 12 -> hour - 12
                        else -> hour
                    }
                    val minuteStr = if (minute < 10) "0$minute" else "$minute"
                    val formattedTime = "$hour12:$minuteStr $amPm"
                    onConfirm(formattedTime)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Select Time") },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(state = timePickerState)
            }
        }
    )
}

// ==========================================
// 4. PREVIEW
// ==========================================
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun PreviewAddMedicineScreen() {
    val dummyState = AddMedicineUiState(
        medicineName = "Paracetamol",
        strength = "500 mg",
        totalTablets = "30",
        dosePerTime = "1 tablet",
        selectedBottleId = R.drawable.buttle, // Set default mock
        times = listOf("8:00 AM", "8:00 PM")
    )
    MaterialTheme {
        AddMedicineContent(
            uiState = dummyState,
            onNameChange = {},
            onStrengthChange = {},
            onTotalTabletsChange = {},
            onDoseChange = {},
            onBottleSelected = {},
            onAddTimeClick = {},
            onSaveClick = {},
            onBackClick = {}
        )
    }
}