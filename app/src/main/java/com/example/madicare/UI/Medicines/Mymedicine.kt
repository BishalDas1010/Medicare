package com.example.madicare.UI.Medicines

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.madicare.R
import com.example.madicare.UI.PrimaryGreen
import com.example.madicare.UI.TextGray

// ==========================================
// 1. UI COLORS
// ==========================================
val ProgressGreen = Color(0xFF34A853)
val ProgressBg = Color(0xFFF3F4F6)
val TextDark = Color(0xFF1A1A1A)
val BorderColor = Color(0xFFF3F4F6)
val ChipSelectedBg = Color(0xFF1A9E56)
val ChipUnselectedBg = Color(0xFFF5F6F8)

// ==========================================
// 2. STATEFUL SCREEN (Hooks up to ViewModel)
// ==========================================
@Composable
fun MyMedicinesScreen(
    viewModel: MedicinesViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onItemClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    MyMedicinesContent(
        uiState = uiState,
        onFilterSelected = viewModel::onFilterSelected,
        onBackClick = onBackClick,
        onAddClick = onAddClick,
        onItemClick = onItemClick
    )
}

// ==========================================
// 3. STATELESS UI CONTENT
// ==========================================
@Composable
fun MyMedicinesContent(
    uiState: MedicinesUiState,
    onFilterSelected: (FilterType) -> Unit,
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    onItemClick: (String) -> Unit = {}
) {
    Scaffold(
        containerColor = Color.White,
        topBar = {
            // Custom Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.clickable { onBackClick() }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "My Medicines",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }

                // Add Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PrimaryGreen)
                        .clickable { onAddClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterType.entries.forEach { filter ->
                    FilterChip(
                        text = filter.label,
                        isSelected = uiState.selectedFilter == filter,
                        onClick = { onFilterSelected(filter) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (uiState.medicines.isEmpty()) {
                EmptyMedicinesState(modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.medicines) { medicine ->
                        MedicineCard(
                            medicine = medicine,
                            onClick = { onItemClick(medicine.id) }
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. SUB-COMPONENTS
// ==========================================
@Composable
fun FilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) ChipSelectedBg else ChipUnselectedBg)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else TextGray,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
fun MedicineCard(medicine: MedicineItem, onClick: () -> Unit = {}) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selected Pill Bottle Icon with pill color background
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(medicine.pillColorHex).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = medicine.bottleDrawable),
                    contentDescription = "Bottle Icon",
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text and Progress Info
            Column(modifier = Modifier.weight(1f)) {
                Text(text = medicine.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text(text = medicine.description, fontSize = 12.sp, color = TextGray)

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${medicine.remaining}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text(text = " / ${medicine.total} remaining", fontSize = 12.sp, color = TextGray)
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Progress Bar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(ProgressBg)
                    ) {
                        val progressColor = when {
                            medicine.progress > 0.5f -> Color(0xFF34A853)
                            medicine.progress > 0.2f -> Color(0xFFFBBC05)
                            else -> Color(0xFFEA4335)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(medicine.progress)
                                .clip(RoundedCornerShape(3.dp))
                                .background(progressColor)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = medicine.percentage,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Chevron Right
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Details",
                tint = TextGray
            )
        }
    }
}

// emapty medicne screen
@Composable
fun EmptyMedicinesState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.finishgray),
            contentDescription = "No medicines",
            modifier = Modifier.size(180.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No medicines yet",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextDark,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "You haven't added any medicines.",
            fontSize = 13.sp,
            color = TextGray,
            textAlign = TextAlign.Center
        )
    }
}

// ==========================================
// 5. PREVIEW
// ==========================================
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun PreviewMyMedicinesScreen() {
    val dummyState = MedicinesUiState(
        medicines = listOf(
            MedicineItem("1", "Paracetamol", "500 mg - 1 tablet daily", 20, 30, com.example.madicare.R.drawable.bluebotttle),
            MedicineItem("2", "Vitamin D3", "1000 IU - 1 tablet daily", 15, 30, com.example.madicare.R.drawable.bottle_orange),
            MedicineItem("3", "BP Medicine", "Amlodipine 5 mg - 1 tablet", 8, 30, com.example.madicare.R.drawable.buttlered)
        )
    )
    MaterialTheme {
        MyMedicinesContent(
            uiState = dummyState,
            onFilterSelected = {},
            onBackClick = {},
            onAddClick = {}
        )
    }
}