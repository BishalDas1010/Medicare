package com.example.madicare.Navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.madicare.UI.Medicines.AddMedicineScreen
import com.example.madicare.UI.Medicines.MyMedicinesScreen
import com.example.madicare.UI.home.MedicineAppScreen

@Composable
fun MainAppScreen() {
    val navController = rememberNavController()

    Scaffold(
        containerColor = Color.White,
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                MedicineAppScreen(
                    onScheduleClick = { navController.navigate(Screen.AddMedicine.route) }
                )
            }
            composable(Screen.Medicines.route) {
                MyMedicinesScreen(
                    onAddClick = { navController.navigate(Screen.AddMedicine.route) }
                )
            }
            composable(Screen.AddMedicine.route) {
                AddMedicineScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.Reports.route) {
                Text("Reports Screen")
            }
            composable(Screen.Family.route) {
                Text("Family Screen")
            }
            composable(Screen.More.route) {
                Text("More Screen")
            }
        }
    }
}
