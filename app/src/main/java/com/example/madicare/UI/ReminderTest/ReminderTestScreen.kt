package com.example.madicare.UI.ReminderTest

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel

private val PrimaryGreen = Color(0xFF00A86B)
private val TextDark = Color(0xFF1E1E1E)
private val TextGray = Color(0xFF757575)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderTestScreen(
    onBackClick: (() -> Unit)? = null,
    viewModel: ReminderTestViewModel = hiltViewModel()
) {
    val testCases by viewModel.testCases.collectAsState()
    val summaryStats by viewModel.summaryStats.collectAsState()
    val liveReminders by viewModel.liveReminders.collectAsState()
    val eventLogs by viewModel.eventLogs.collectAsState()
    val isAutoWorkflowRunning by viewModel.isAutoWorkflowRunning.collectAsState()
    val workflowMessage by viewModel.workflowMessage.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Dashboard & Tests", "Manual Controls", "Live Reminders", "Event Logs")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reminder Test Center 🧪", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = TextDark
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Summary Card at top
            SummaryCard(
                stats = summaryStats,
                onRunAll = { viewModel.runAllTests() },
                onRunAutoWorkflow = { viewModel.runAutomatedWorkflowTest() },
                onClearLogs = { viewModel.clearLogs() }
            )

            AnimatedVisibility(visible = isAutoWorkflowRunning) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PrimaryGreen))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = PrimaryGreen, strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = workflowMessage, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    }
                }
            }

            // Tab Navigation
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = PrimaryGreen,
                edgePadding = 16.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> TestDashboardSection(
                    testCases = testCases,
                    onRunSingleTest = { viewModel.runTest(it) }
                )
                1 -> ManualControlsSection(
                    onTestNotifNow = { viewModel.testNotificationNow() },
                    onTestAlarm10s = { viewModel.testAlarmAfter10Seconds() },
                    onTestPreReminder10s = { viewModel.testPreReminderAfter10Seconds() },
                    onTestSnooze10s = { viewModel.testSnoozeAfter10Seconds() },
                    onCancelAllAlarms = { viewModel.cancelAllTestAlarms() }
                )
                2 -> LiveRemindersSection(
                    reminders = liveReminders,
                    onRefresh = { viewModel.refreshLiveReminders() }
                )
                3 -> EventLogsSection(logs = eventLogs)
            }
        }
    }
}

@Composable
fun SummaryCard(
    stats: SummaryStats,
    onRunAll: () -> Unit,
    onRunAutoWorkflow: () -> Unit,
    onClearLogs: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Overall Status", fontSize = 14.sp, color = TextGray)
                    Text("${stats.successPercentage}% Success", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Last Execution", fontSize = 11.sp, color = TextGray)
                    Text(stats.lastExecutionTime, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextDark)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { stats.successPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = PrimaryGreen,
                trackColor = Color(0xFFE0E0E0),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatChip(label = "Total: ${stats.total}", color = Color.DarkGray)
                StatChip(label = "Passed: ${stats.passed}", color = Color(0xFF2E7D32))
                StatChip(label = "Failed: ${stats.failed}", color = Color(0xFFC62828))
                StatChip(label = "Not Tested: ${stats.notTested}", color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onRunAll,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Run All", fontSize = 12.sp)
                }

                Button(
                    onClick = onRunAutoWorkflow,
                    modifier = Modifier.weight(1.3f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Auto Workflow", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onClearLogs,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun StatChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), shape = RoundedCornerShape(12.dp))
            .border(1.dp, color.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
fun TestDashboardSection(
    testCases: List<TestCase>,
    onRunSingleTest: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(testCases, key = { it.id }) { testCase ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(testCase.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                        Text(testCase.description, fontSize = 11.sp, color = TextGray)

                        if (testCase.executionTimeMs > 0) {
                            Text("Execution time: ${testCase.executionTimeMs} ms", fontSize = 10.sp, color = Color.Gray)
                        }

                        if (!testCase.errorMessage.isNullOrBlank()) {
                            Text("Error: ${testCase.errorMessage}", fontSize = 11.sp, color = Color.Red, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(horizontalAlignment = Alignment.End) {
                        StatusBadge(status = testCase.status)
                        Spacer(modifier = Modifier.height(6.dp))
                        IconButton(
                            onClick = { onRunSingleTest(testCase.id) },
                            modifier = Modifier
                                .size(32.dp)
                                .background(PrimaryGreen.copy(alpha = 0.15f), shape = CircleShape)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Run", tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: TestStatus) {
    val (bgColor, textColor, text) = when (status) {
        TestStatus.NOT_TESTED -> Triple(Color(0xFFEEEEEE), Color.Gray, "NOT TESTED")
        TestStatus.RUNNING -> Triple(Color(0xFFE3F2FD), Color(0xFF1976D2), "RUNNING")
        TestStatus.PASSED -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "PASSED")
        TestStatus.FAILED -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "FAILED")
    }

    Row(
        modifier = Modifier
            .background(bgColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (status == TestStatus.RUNNING) {
            CircularProgressIndicator(modifier = Modifier.size(10.dp), strokeWidth = 2.dp, color = textColor)
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(text = text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

@Composable
fun ManualControlsSection(
    onTestNotifNow: () -> Unit,
    onTestAlarm10s: () -> Unit,
    onTestPreReminder10s: () -> Unit,
    onTestSnooze10s: () -> Unit,
    onCancelAllAlarms: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Manual Developer Testing Controls", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
        Text("These controls test system alarm & notification functions using safe test IDs.", fontSize = 12.sp, color = TextGray)

        ManualButton(title = "🔔 Test Notification Now", description = "Triggers instant notification with Action buttons", onClick = onTestNotifNow)
        ManualButton(title = "⏱️ Test Alarm After 10 Seconds", description = "Schedules an exact AlarmManager alarm for +10 seconds", onClick = onTestAlarm10s)
        ManualButton(title = "⏰ Test Pre-Reminder After 10 Seconds", description = "Schedules pre-reminder notification for +10 seconds", onClick = onTestPreReminder10s)
        ManualButton(title = "💤 Test Snooze After 10 Seconds", description = "Schedules snooze reminder for +10 seconds", onClick = onTestSnooze10s)
        ManualButton(title = "🚫 Cancel All Test Alarms", description = "Cancels all active test alarms instantly", isDanger = true, onClick = onCancelAllAlarms)
    }
}

@Composable
fun ManualButton(
    title: String,
    description: String,
    isDanger: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isDanger) Color.Red else TextDark)
                Text(description, fontSize = 11.sp, color = TextGray)
            }
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = if (isDanger) Color(0xFFC62828) else PrimaryGreen)
            ) {
                Text("Trigger", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun LiveRemindersSection(
    reminders: List<LiveReminderItem>,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Live Scheduled Reminders", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = PrimaryGreen)
            }
        }

        if (reminders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No active scheduled reminders found.\nUse Manual Controls to schedule a test alarm.", color = TextGray, fontSize = 13.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(reminders, key = { it.pendingIntentId }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(item.medicineName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(item.status, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryGreen)
                            }
                            Text("Scheduled Time: ${item.scheduledTimeFormatted}", fontSize = 12.sp, color = TextGray)
                            Text("Type: ${item.alarmType} | Snooze Count: ${item.snoozeCount}", fontSize = 11.sp, color = TextGray)
                            Text("PendingIntent ID: ${item.pendingIntentId}", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventLogsSection(logs: List<EventLogItem>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Event Logs (${logs.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
        Spacer(modifier = Modifier.height(8.dp))

        if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No event logs recorded yet.", color = TextGray, fontSize = 13.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(logs, key = { it.id }) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (log.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (log.isSuccess) Color(0xFF2E7D32) else Color(0xFFC62828),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(log.eventType, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextDark)
                                    Text(log.timestampFormatted, fontSize = 10.sp, color = TextGray)
                                }
                                Text(log.details, fontSize = 11.sp, color = TextGray)
                            }
                        }
                    }
                }
            }
        }
    }
}
