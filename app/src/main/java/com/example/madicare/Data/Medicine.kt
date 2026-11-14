package com.example.madicare.Data

import com.example.madicare.Ennum.MedicineStatus


data class Medicine(
    val id: String,
    val name: String,
    val dosage: String,
    val time: String,
    val status: MedicineStatus
)