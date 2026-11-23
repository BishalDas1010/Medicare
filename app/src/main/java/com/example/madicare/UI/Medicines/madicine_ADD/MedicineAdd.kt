package com.example.madicare.UI.Medicines.madicine_ADD

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.madicare.R

// ==========================================
// COLORS & CONSTANTS
// ==========================================
val PrimaryGreen = Color(0xFF0F8A43)
val LightGreenBg = Color(0xFFF0F9F2)
val FieldBgColor = Color(0xFFF8F8F8)
val TextDark = Color(0xFF1E1E1E)
val TextGray = Color(0xFF8E8E93)
val BorderColor = Color(0xFFE5E5EA)

val TabletColorOptions = listOf(
    0xFFFFFFFF, // White
    0xFFFF8A80, // Pink
    0xFFFFA500, // Orange
    0xFFFFD54F, // Yellow
    0xFF64B5F6, // Blue
    0xFFBA68C8  // Purple
)

val MedicineIllustrations = listOf(
    R.drawable.bluebotttle,
    R.drawable.bottle_orange,
    R.drawable.bottle_yello,
    R.drawable.buttle_blur,
    R.drawable.buttle_pink,
    R.drawable.buttlepuerple,
    R.drawable.buttlered,
    R.drawable.finishgray,
    R.drawable.orangebottole,
    R.drawable.yellobottle,
    R.drawable.yelloishorange,
    R.drawable.oragewithleaf
)

val WeekDaysList = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
val DosageForms = listOf("Tablet", "Capsule", "Syrup", "Drops", "Injection", "Other")

// ==========================================
// STATEFUL ENTRY POINT
// ==========================================
@Composable
fun AddMedicineScreen(
    viewModel: AddMedicineViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showTimePicker by remember { mutableStateOf(false) }
    var showBottlePicker by remember { mutableStateOf(false) }

    if (showTimePicker) {
        CustomTimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { time ->
                viewModel.addDoseTime(time)
                showTimePicker = false
            }
        )
    }

    if (showBottlePicker) {
        BottlePickerDialog(
            selectedId = uiState.selectedBottleId,
            onDismiss = { showBottlePicker = false },
            onSelect = { id ->
                viewModel.onBottleSelected(id)
                showBottlePicker = false
            }
        )
    }

    AddMedicineContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onNameChange = viewModel::onNameChange,
        onStrengthChange = viewModel::onStrengthChange,
        onDosageFormChange = viewModel::onDosageFormChange,
        onQuantityChange = viewModel::onTotalQuantityChange,
        onBottleClick = { showBottlePicker = true },
        onColorSelected = viewModel::onColorSelected,
        onTakeAmountChange = viewModel::onTakeAmountChange,
        onTimesPerDayChange = viewModel::onTimesPerDayChange,
        onFrequencyChange = viewModel::onFrequencyChange,
        onToggleDay = viewModel::toggleDay,
        onAddTimeClick = { showTimePicker = true },
        onRemoveTime = viewModel::removeDoseTime,
        onDurationChange = viewModel::onTreatmentDurationChange,
        onNotesChange = viewModel::onNotesChange,
        onSaveClick = { viewModel.saveMedicine(onBackClick) }
    )
}

// ==========================================
// STATELESS UI CONTENT
// ==========================================
@Composable
fun AddMedicineContent(
    uiState: AddMedicineUiState,
    onBackClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onStrengthChange: (String) -> Unit,
    onDosageFormChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onBottleClick: () -> Unit,
    onColorSelected: (Long) -> Unit,
    onTakeAmountChange: (Int) -> Unit,
    onTimesPerDayChange: (Int) -> Unit,
    onFrequencyChange: (String) -> Unit,
    onToggleDay: (String) -> Unit,
    onAddTimeClick: () -> Unit,
    onRemoveTime: (String) -> Unit,
    onDurationChange: (Int) -> Unit,
    onNotesChange: (String) -> Unit,
    onSaveClick: () -> Unit
) {
    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = onSaveClick,
                    enabled = !uiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Medicine", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
            Spacer(modifier = Modifier.height(16.dp))

            // --- Top Bar ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark)
                }
                Text("Add Medicine", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Error Message Banner ---
            if (uiState.errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFDEDEC)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.ErrorOutline, contentDescription = "Error", tint = Color(0xFFC0392B))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.errorMessage,
                            color = Color(0xFFC0392B),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // --- Medicine Information ---
            SectionHeader(icon = Icons.Outlined.Medication, title = "Medicine Information")

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Clickable Illustration Box with Pill Background Color
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(uiState.selectedColor).copy(alpha = 0.25f))
                        .border(2.dp, Color(uiState.selectedColor), RoundedCornerShape(18.dp))
                        .clickable { onBottleClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = uiState.selectedBottleId),
                        contentDescription = "Selected Illustration",
                        modifier = Modifier.size(54.dp)
                    )
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Change Illustration",
                        tint = PrimaryGreen,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .size(20.dp)
                            .background(Color.White, CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedTextFieldWithIcon(
                    icon = Icons.Outlined.Medication,
                    label = "Medicine Name *",
                    value = uiState.medicineName,
                    onValueChange = onNameChange,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextFieldWithIcon(
                    icon = Icons.Outlined.Scale,
                    label = "Strength (e.g. 500 mg)",
                    value = uiState.strength,
                    onValueChange = onStrengthChange,
                    modifier = Modifier.weight(1f)
                )
                DosageFormDropdown(
                    selectedForm = uiState.dosageForm,
                    onFormSelected = onDosageFormChange,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextFieldWithIcon(
                    icon = Icons.Outlined.Layers,
                    label = "Total Quantity *",
                    value = uiState.totalQuantity,
                    onValueChange = onQuantityChange,
                    modifier = Modifier.weight(1f),
                    trailingIconText = uiState.unit
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text("Pill Color", fontSize = 12.sp, color = TextGray, modifier = Modifier.padding(bottom = 8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TabletColorOptions.forEach { colorHex ->
                            val isSelected = colorHex == uiState.selectedColor
                            val color = Color(colorHex)
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) PrimaryGreen else BorderColor,
                                        shape = CircleShape
                                    )
                                    .clickable { onColorSelected(colorHex) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (colorHex == 0xFFFFFFFFL) PrimaryGreen else Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Daily Intake (Dosing) ---
            Card(
                colors = CardDefaults.cardColors(containerColor = LightGreenBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Medication, contentDescription = null, tint = PrimaryGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Daily Intake (Dosing)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                                Text("Dosage per intake and daily frequency", fontSize = 12.sp, color = TextGray)
                            }
                        }
                        Surface(
                            color = PrimaryGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Total: ${uiState.totalDailyConsumption} ${uiState.unit.lowercase()}/day",
                                fontSize = 11.sp,
                                color = PrimaryGreen,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dose", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.width(8.dp))

                        // Stepper
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(20.dp))
                                .border(1.dp, BorderColor, RoundedCornerShape(20.dp))
                        ) {
                            IconButton(onClick = { onTakeAmountChange(-1) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                            }
                            Text("${uiState.takeAmount}", modifier = Modifier.padding(horizontal = 6.dp), fontWeight = FontWeight.Bold)
                            IconButton(onClick = { onTakeAmountChange(1) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp), tint = PrimaryGreen)
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))
                        Text(uiState.dosageForm.lowercase(), fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(8.dp))

                        // Times per day dropdown
                        TimesPerDayDropdown(
                            selectedCount = uiState.timesPerDayCount,
                            onCountSelected = onTimesPerDayChange,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Medicine Schedule ---
            SectionHeader(icon = Icons.Outlined.CalendarMonth, title = "Medicine Schedule")

            Text("Frequency", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))

            val freqs = listOf("Every day", "Specific days", "Weekly", "Monthly", "Custom")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(freqs) { freq ->
                    val isSelected = uiState.frequency == freq
                    Surface(
                        color = if (isSelected) PrimaryGreen else Color.White,
                        shape = RoundedCornerShape(20.dp),
                        border = if (!isSelected) BorderStroke(1.dp, BorderColor) else null,
                        modifier = Modifier.clickable { onFrequencyChange(freq) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                freq,
                                fontSize = 12.sp,
                                color = if (isSelected) Color.White else TextDark,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Specific Days Selector
            if (uiState.frequency == "Specific days" || uiState.frequency == "Custom") {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LightGreenBg, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text("Select Days:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        WeekDaysList.forEach { day ->
                            val isDaySelected = uiState.selectedDays.contains(day)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isDaySelected) PrimaryGreen else Color.White)
                                    .border(1.dp, if (isDaySelected) PrimaryGreen else BorderColor, CircleShape)
                                    .clickable { onToggleDay(day) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    day,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDaySelected) Color.White else TextDark
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dose Times Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Schedule, contentDescription = null, modifier = Modifier.size(18.dp), tint = PrimaryGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Dose Times *", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(uiState.doseTimes) { time ->
                    TimeChip(text = time, onRemove = { onRemoveTime(time) })
                }
                item {
                    OutlinedButton(
                        onClick = onAddTimeClick,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, PrimaryGreen),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Dose Time", color = PrimaryGreen, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Treatment Duration ---
            SectionHeader(icon = Icons.Outlined.CalendarMonth, title = "Treatment Duration")

            Card(
                colors = CardDefaults.cardColors(containerColor = FieldBgColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.clickable { onDurationChange(0) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.treatmentDurationOption == 0,
                            onClick = { onDurationChange(0) },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen)
                        )
                        Column {
                            Text("Until medicine runs out", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Text("Estimated completion date: ${uiState.calculatedEndDate}", fontSize = 12.sp, color = TextGray)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.clickable { onDurationChange(1) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.treatmentDurationOption == 1,
                            onClick = { onDurationChange(1) },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen)
                        )
                        Column {
                            Text("Set treatment duration", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Text("Calculated end date: ${uiState.calculatedEndDate}", fontSize = 12.sp, color = TextGray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Notes ---
            Row {
                SectionHeader(icon = Icons.Outlined.Description, title = "Notes")
                Text(" (optional)", color = TextGray, fontSize = 14.sp, modifier = Modifier.padding(top = 12.dp))
            }

            OutlinedTextField(
                value = uiState.notes,
                onValueChange = onNotesChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = FieldBgColor,
                    focusedContainerColor = FieldBgColor,
                    unfocusedBorderColor = BorderColor,
                    focusedBorderColor = PrimaryGreen
                ),
                placeholder = { Text("Add special instructions (e.g. take after meals)", fontSize = 13.sp, color = TextGray) }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ==========================================
// SUB-COMPONENTS & DIALOGS
// ==========================================

@Composable
fun SectionHeader(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
    }
}

@Composable
fun OutlinedTextFieldWithIcon(
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    trailingIconText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = FieldBgColor,
            focusedContainerColor = FieldBgColor,
            unfocusedBorderColor = BorderColor,
            focusedBorderColor = PrimaryGreen
        ),
        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, color = TextDark),
        singleLine = true,
        label = { Text(label, fontSize = 12.sp) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = TextGray, modifier = Modifier.size(18.dp)) },
        trailingIcon = {
            if (trailingIconText != null) {
                Text(trailingIconText, fontSize = 12.sp, color = TextDark, modifier = Modifier.padding(end = 8.dp))
            }
        }
    )
}

@Composable
fun DosageFormDropdown(
    selectedForm: String,
    onFormSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedForm,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = FieldBgColor,
                focusedContainerColor = FieldBgColor,
                unfocusedBorderColor = BorderColor,
                focusedBorderColor = PrimaryGreen
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, color = TextDark),
            singleLine = true,
            label = { Text("Dosage Form *", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Outlined.Medication, contentDescription = null, tint = TextGray, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Select Form", tint = TextGray)
                }
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            DosageForms.forEach { form ->
                DropdownMenuItem(
                    text = { Text(form, fontSize = 14.sp, color = TextDark) },
                    onClick = {
                        onFormSelected(form)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun TimesPerDayDropdown(
    selectedCount: Int,
    onCountSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(1, 2, 3, 4, 5, 6)

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("$selectedCount time(s) / day", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextDark)
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextGray, modifier = Modifier.size(18.dp))
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            options.forEach { count ->
                DropdownMenuItem(
                    text = { Text("$count time(s) a day", fontSize = 14.sp, color = TextDark) },
                    onClick = {
                        onCountSelected(count)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun TimeChip(text: String, onRemove: () -> Unit) {
    Surface(
        color = LightGreenBg,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Schedule, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = text, fontSize = 12.sp, color = TextDark, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = TextDark,
                modifier = Modifier
                    .size(14.dp)
                    .clickable { onRemove() }
            )
        }
    }
}

@Composable
fun BottlePickerDialog(
    selectedId: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Select Medicine Illustration",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                items(MedicineIllustrations) { bottleId ->
                    val isSelected = bottleId == selectedId
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) LightGreenBg else FieldBgColor)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) PrimaryGreen else BorderColor,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onSelect(bottleId) },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = bottleId),
                            contentDescription = "Illustration Option",
                            modifier = Modifier.size(46.dp)
                        )
                        if (isSelected) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = PrimaryGreen,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = PrimaryGreen, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val timePickerState = rememberTimePickerState(is24Hour = false)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Alarm, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Select Dose Time", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    color = LightGreenBg,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val hour = if (timePickerState.hour == 0) 12 else if (timePickerState.hour > 12) timePickerState.hour - 12 else timePickerState.hour
                        val minute = if (timePickerState.minute < 10) "0${timePickerState.minute}" else "${timePickerState.minute}"
                        val amPm = if (timePickerState.hour >= 12) "PM" else "AM"

                        Text("$hour : $minute", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(amPm, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                    }
                }

                TimePicker(state = timePickerState)
            }
        },
        confirmButton = {
            Button(
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
                    onConfirm("$hour12:$minuteStr $amPm")
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Add Time")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = PrimaryGreen)
            }
        }
    )
}

// ==========================================
// PREVIEW
// ==========================================
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun PreviewAddMedicine() {
    MaterialTheme {
        AddMedicineContent(
            uiState = AddMedicineUiState(),
            onBackClick = {},
            onNameChange = {},
            onStrengthChange = {},
            onDosageFormChange = {},
            onQuantityChange = {},
            onBottleClick = {},
            onColorSelected = {},
            onTakeAmountChange = {},
            onTimesPerDayChange = {},
            onFrequencyChange = {},
            onToggleDay = {},
            onAddTimeClick = {},
            onRemoveTime = {},
            onDurationChange = {},
            onNotesChange = {},
            onSaveClick = {}
        )
    }
}
