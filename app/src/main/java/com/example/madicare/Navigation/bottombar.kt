package com.example.madicare.Navigation
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

import com.example.madicare.ui.theme.*

// 1. Define your Navigation Routes
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Medicines : Screen("medicines", "Medicines", Icons.Outlined.DateRange)
    object Reports : Screen("reports", "Reports", Icons.Outlined.Assignment)
    object Family : Screen("family", "Family", Icons.Outlined.People)
    object More : Screen("more", "More", Icons.Outlined.MoreHoriz)
    object AddMedicine : Screen("add_medicine?medicineId={medicineId}", "Add Medicine", Icons.Filled.Home) {
        fun createRoute(medicineId: Long = -1L) = "add_medicine?medicineId=$medicineId"
    }
    object MedicineDetails : Screen("medicine_details/{medicineId}", "Medicine Details", Icons.Filled.Home) {
        fun createRoute(medicineId: String) = "medicine_details/$medicineId"
    }
    object ReminderTest : Screen("reminder_test", "Test Center", Icons.Outlined.Assignment)
    object Settings : Screen("settings", "Settings", Icons.Outlined.Settings)
}

// List of all tabs
val bottomNavItems = listOf(
    Screen.Home,
    Screen.Medicines,
    Screen.Reports,
    Screen.Family,
    Screen.More
)

// 2. The updated Bottom Navigation Bar
@Composable
fun BottomNavigationBar(navController: NavController) {
    // Listen to the current navigation state to know which tab is selected
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp,
        modifier = Modifier.border(1.dp, BorderColor, RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
    ) {
        bottomNavItems.forEach { screen ->
            val isSelected = currentRoute == screen.route

            NavigationBarItem(
                icon = { Icon(screen.icon,
                modifier = Modifier.size(30.dp),
                    contentDescription = screen.title) },
                label = {
                    Text(
                        text = screen.title,
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                selected = isSelected, // Dynamically set based on current route
                onClick = {
                    if (!isSelected) {
                        // Navigate to the selected screen
                        navController.navigate(screen.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when reselecting
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryGreen,
                    selectedTextColor = PrimaryGreen,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}





@Preview(showSystemUi = true,
    showBackground = true)
@Composable
fun PreviewBottomNavigationBar() {
    // 1. Create a mock NavController for the preview
    val mockNavController = rememberNavController()

    // 2. Wrap it in a Scaffold so the bottom bar sits at the bottom
    Scaffold(
        bottomBar = {
            // Pass the mock controller to your actual BottomNavigationBar
            BottomNavigationBar(navController = mockNavController)
        }
    ) { paddingValues ->
        // 3. Provide a dummy NavHost so the controller has a graph to look at
        NavHost(
            navController = mockNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Create dummy destinations for each screen so the preview knows they exist
            composable(Screen.Home.route) { Text("Home Screen") }
            composable(Screen.Medicines.route) { Text("Medicines Screen") }
            composable(Screen.Reports.route) { Text("Reports Screen") }
            composable(Screen.Family.route) { Text("Family Screen") }
            composable(Screen.More.route) { Text("More Screen") }
        }
    }
}