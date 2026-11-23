package com.example.madicare.Navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.madicare.UI.Medicines.MyMedicinesScreen
import com.example.madicare.UI.Medicines.madicine_ADD.AddMedicineScreen
import com.example.madicare.UI.home.MedicineAppScreen
import com.example.madicare.UI.MedicineDetails.MedicineDetailsScreen

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
                    onScheduleClick = { navController.navigate(Screen.AddMedicine.createRoute()) },
                    onItemClick = { medicineId -> navController.navigate(Screen.MedicineDetails.createRoute(medicineId)) }
                )
            }
            composable(Screen.Medicines.route) {
                MyMedicinesScreen(
                    onAddClick = { navController.navigate(Screen.AddMedicine.createRoute()) },
                    onItemClick = { medicineId -> navController.navigate(Screen.MedicineDetails.createRoute(medicineId)) },

                    onBackClick = {
                        navController.navigate(Screen.Home.route)
                    }
                )
            }
            composable(
                route = Screen.AddMedicine.route,
                arguments = listOf(navArgument("medicineId") {
                    type = NavType.LongType
                    defaultValue = -1L
                })
            ) {
                AddMedicineScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.MedicineDetails.route,
                arguments = listOf(navArgument("medicineId") { type = NavType.StringType })
            ) {
                MedicineDetailsScreen(
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { medicineId -> navController.navigate(Screen.AddMedicine.createRoute(medicineId.toLongOrNull() ?: -1L)) },
                    onDeleted = { navController.popBackStack() }
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
