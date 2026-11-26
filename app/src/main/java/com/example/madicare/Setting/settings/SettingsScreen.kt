package com.example.madicare.Setting.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.madicare.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userName: String = "Bishal Das",
    reminderSound: String = "Nature",
    theme: String = "System",
    backupSync: String = "On",
    appLock: String = "Off",
    language: String = "English",
    appData: String = "Appdata",
    onBackClick: () -> Unit = {},
    onAccountClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onReminderSoundClick: () -> Unit = {},
    onThemeClick: () -> Unit = {},
    onBackupSyncClick: () -> Unit = {},
    onAppLockClick: () -> Unit = {},
    onLanguageClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onClearDataClick: () -> Unit = {},
    onNavigateToTestCenter: () -> Unit = {} // kept for compatibility with existing navigation
) {
    // Local state: null = show settings list, non-null = show placeholder detail
    var selectedSetting by remember { mutableStateOf<String?>(null) }

    if (selectedSetting != null) {
        PlaceholderDetailScreen(
            title = selectedSetting!!,
            onBack = { selectedSetting = null }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextMain
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ScreenBg,
                    titleContentColor = TextMain
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = ScreenBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Account card
            AccountCard(name = userName, onClick = onAccountClick)

            // App Preferences
            SectionHeader("App Preferences")



            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = TextMain,
                    modifier = Modifier.size(30.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Notification",
                    modifier = Modifier.weight(1f)
                )
                var isToggled by rememberSaveable { mutableStateOf(false) }

                Switch(
                    checked = isToggled,
                    onCheckedChange = { isToggled = it }
                )
            }


            SettingRow(
                Icons.Default.VolumeUp,
                "Reminder Sound",
                reminderSound,
                onClick = {
                    onReminderSoundClick()
                    selectedSetting = "Reminder Sound"
                }
            )
            SettingRow(
                Icons.Default.Palette,
                "Theme",
                theme,
                onClick = {
                    onThemeClick()
                    selectedSetting = "Theme"
                }
            )

            // Data & Sync
            SectionHeader("Data & Sync")
            SettingRow(
                Icons.Default.Sync,
                "Backup & Sync",
                backupSync,
                onClick = {
                    onBackupSyncClick()
                    selectedSetting = "Backup & Sync"
                }
            )

            // Security
            SectionHeader("Security")
            SettingRow(
                Icons.Default.Lock,
                "App Lock",
                appLock,
                onClick = {
                    onAppLockClick()
                    selectedSetting = "App Lock"
                }
            )
            SettingRow(
                Icons.Default.Language,
                "Language",
                language,
                onClick = {
                    onLanguageClick()
                    selectedSetting = "Language"
                }
            )
            SettingRow(
                Icons.Default.Delete,
                "Clear all data",
                appData,
                onClick = {
                    onClearDataClick()
                    selectedSetting = "Clear all data"
                }
            )

            // About
            SectionHeader("About")
            SettingRow(
                Icons.Default.Info,
                "About MediCare",
                onClick = {
                    onAboutClick()
                    selectedSetting = "About MediCare"
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceholderDetailScreen(
    title: String,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextMain
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ScreenBg,
                    titleContentColor = TextMain
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = ScreenBg
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "This screen is coming soon",
                    fontSize = 14.sp,
                    color = TextSub
                )
            }
        }
    }
}

@Composable
private fun AccountCard(name: String, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(LightGreenBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Account",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextMain
                )
                Text(name, fontSize = 12.sp, color = TextSub)
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextMain,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = TextMain,
        modifier = Modifier.padding(start = 2.dp, top = 8.dp)
    )
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    value: String? = null,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                icon,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextMain,
                modifier = Modifier.weight(1f)
            )

            if (value != null) {
                Text(value, fontSize = 13.sp, color = TextSub)
                Spacer(modifier = Modifier.width(2.dp))
            }

            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextMain,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    MaterialTheme {
        SettingsScreen()
    }
}