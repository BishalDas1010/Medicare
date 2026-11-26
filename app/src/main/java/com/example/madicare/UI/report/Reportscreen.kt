package com.example.madicare.UI.report

import com.example.madicare.ui.theme.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
// ==========================================
// MODELS
// ==========================================
enum class ReportPeriod(val label: String) {
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year")
}

private enum class DownloadState {
    IDLE,
    LOADING,
    DONE
}

data class MedicineAdherence(
    val name: String,
    val taken: Int,
    val total: Int
) {
    val percent: Int get() = if (total == 0) 0 else (taken * 100f / total).roundToInt()
}

data class ReportData(
    val adherence: Int,
    val trend: Int,                 // change vs previous period, in %
    val compareLabel: String,       // e.g. "vs last week"
    val periodLabel: String,        // e.g. "This week"
    val labels: List<String>,
    val points: List<Float>,        // 0f..1f, same size as labels
    val taken: Int,
    val missed: Int,
    val streak: Int,
    val bestStreak: Int,
    val medicines: List<MedicineAdherence>,
    val insight: String
)

// ==========================================
// SAMPLE DATA (replace with your ViewModel / database data)
// ==========================================
fun sampleReportData(period: ReportPeriod): ReportData = when (period) {
    ReportPeriod.WEEK -> ReportData(
        adherence = 84, trend = 6, compareLabel = "vs last week", periodLabel = "This week",
        labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
        points = listOf(0.70f, 0.85f, 0.95f, 0.60f, 0.90f, 0.75f, 0.92f),
        taken = 32, missed = 6, streak = 6, bestStreak = 12,
        medicines = listOf(
            MedicineAdherence("Metformin 500mg", 13, 14),
            MedicineAdherence("Amlodipine 5mg", 11, 12),
            MedicineAdherence("Vitamin D3", 8, 12)
        ),
        insight = "Most missed doses happen on Thursday. Try setting a pre-reminder 15 minutes earlier."
    )
    ReportPeriod.MONTH -> ReportData(
        adherence = 84, trend = 3, compareLabel = "vs last month", periodLabel = "This month",
        labels = listOf("W1", "W2", "W3", "W4"),
        points = listOf(0.78f, 0.88f, 0.72f, 0.90f),
        taken = 128, missed = 24, streak = 6, bestStreak = 21,
        medicines = listOf(
            MedicineAdherence("Metformin 500mg", 52, 56),
            MedicineAdherence("Amlodipine 5mg", 44, 48),
            MedicineAdherence("Vitamin D3", 32, 48)
        ),
        insight = "Week 3 was your lowest. Vitamin D3 is the dose you skip most often."
    )
    ReportPeriod.YEAR -> ReportData(
        adherence = 84, trend = 9, compareLabel = "vs last year", periodLabel = "This year",
        labels = listOf("J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D"),
        points = listOf(0.62f, 0.68f, 0.74f, 0.70f, 0.80f, 0.84f, 0.78f, 0.88f, 0.86f, 0.92f, 0.85f, 0.90f),
        taken = 1520, missed = 280, streak = 6, bestStreak = 45,
        medicines = listOf(
            MedicineAdherence("Metformin 500mg", 630, 672),
            MedicineAdherence("Amlodipine 5mg", 520, 600),
            MedicineAdherence("Vitamin D3", 370, 528)
        ),
        insight = "Your adherence improved steadily this year. Keep your streak going to beat your best."
    )
}



// ==========================================
// COLOR ALIASES FROM CENTRAL THEME
// ==========================================
private val Green = PrimaryGreen
private val GreenDark = PrimaryGreenDark
private val GreenSoft = LightGreenSoft
private val Red = DangerRed
private val Amber = WarningAmber

private fun adherenceColor(percent: Int): Color = when {
    percent >= 80 -> Green
    percent >= 60 -> Amber
    else -> Red
}

// ==========================================
// SCREEN
// ==========================================
@Composable
fun HealthReportScreen(
    onBackClick: (() -> Unit)? = null,
    onDownloadClick: () -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val reportData by viewModel.reportData.collectAsStateWithLifecycle()
    val period by viewModel.selectedPeriod.collectAsStateWithLifecycle()

    HealthReportScreenContent(
        onBackClick = onBackClick,
        onDownloadClick = onDownloadClick,
        period = period,
        data = reportData,
        onPeriodSelected = { viewModel.selectPeriod(it) }
    )
}

@Composable
fun HealthReportScreenContent(
    onBackClick: (() -> Unit)? = null,
    onDownloadClick: () -> Unit = {},
    period: ReportPeriod = ReportPeriod.WEEK,
    data: ReportData = sampleReportData(period),
    onPeriodSelected: (ReportPeriod) -> Unit = {}
) {
    var downloadState by remember { mutableStateOf(DownloadState.IDLE) }

    LaunchedEffect(downloadState) {
        when (downloadState) {
            DownloadState.LOADING -> {
                delay(1500)
                downloadState = DownloadState.DONE
            }
            DownloadState.DONE -> {
                delay(2000)
                downloadState = DownloadState.IDLE
            }
            DownloadState.IDLE -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 8.dp, end = 16.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextDark)
                }
            } else {
                Spacer(modifier = Modifier.width(8.dp))
            }
            Column {
                Text("Health Reports", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                Text("Your medication insights", fontSize = 12.sp, color = TextGray)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        PeriodSelector(
            selected = period,
            onSelected = onPeriodSelected,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Staggered(0) { AdherenceCard(data) } }
            item {
                Staggered(1) {
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        StatCard(
                            label = "Doses Taken",
                            value = data.taken,
                            accent = Green,
                            tint = GreenSoft,
                            icon = Icons.Default.CheckCircle,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Missed Doses",
                            value = data.missed,
                            accent = Red,
                            tint = RedSoft,
                            icon = Icons.Default.Close,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            item {
                Staggered(2) {
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        InfoTile("🔥", "${data.streak} days", "Current streak", Modifier.weight(1f))
                        InfoTile("🏆", "${data.bestStreak} days", "Best streak", Modifier.weight(1f))
                    }
                }
            }
            item { Staggered(3) { MedicineBreakdownCard(data.medicines) } }
            item { Staggered(4) { InsightCard(data.insight) } }
        }

        // Bottom bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp)
                .background(Color.White)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            DownloadButton(
                state = downloadState,
                onClick = {
                    downloadState = DownloadState.LOADING
                    onDownloadClick()
                }
            )
        }
    }
}

// ==========================================
// PERIOD SELECTOR (sliding pill)
// ==========================================
@Composable
private fun PeriodSelector(
    selected: ReportPeriod,
    onSelected: (ReportPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFE5ECE9))
            .padding(4.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val segmentWidth = maxWidth / ReportPeriod.entries.size
            val offsetX by animateDpAsState(
                targetValue = segmentWidth * selected.ordinal,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "pill"
            )

            Box(
                modifier = Modifier
                    .offset(x = offsetX)
                    .width(segmentWidth)
                    .height(42.dp)
                    .shadow(3.dp, RoundedCornerShape(14.dp))
                    .background(Color.White, RoundedCornerShape(14.dp))
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                ReportPeriod.entries.forEach { item ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onSelected(item) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.label,
                            fontSize = 14.sp,
                            fontWeight = if (item == selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (item == selected) GreenDark else TextGray
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// ADHERENCE CARD + INTERACTIVE CHART
// ==========================================
@Composable
private fun AdherenceCard(data: ReportData) {
    val animated by animateIntAsState(data.adherence, tween(900), label = "adherence")

    ReportCard {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text("Medicine Adherence", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text(data.periodLabel, fontSize = 12.sp, color = TextGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    TrendChip(trend = data.trend, compareLabel = data.compareLabel)
                }
                Text(
                    text = "$animated%",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = adherenceColor(data.adherence)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AdherenceChart(points = data.points, labels = data.labels)
        }
    }
}

@Composable
private fun TrendChip(trend: Int, compareLabel: String) {
    val positive = trend >= 0
    val color = if (positive) Green else Red
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (positive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "${kotlin.math.abs(trend)}% $compareLabel",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun AdherenceChart(points: List<Float>, labels: List<String>) {
    var selected by remember(points) { mutableStateOf(points.lastIndex) }
    val reveal = remember(points) { Animatable(0f) }
    LaunchedEffect(points) {
        reveal.animateTo(1f, tween(1000, easing = FastOutSlowInEasing))
    }

    val safeIndex = selected.coerceIn(0, points.lastIndex)
    val selectedPercent = (points[safeIndex] * 100).roundToInt()

    Column {
        // Selected value chip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Tap or drag to explore", fontSize = 11.sp, color = TextGray)
            Text(
                text = "${labels[safeIndex]} · $selectedPercent%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(GreenDark)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .pointerInput(points) {
                    detectTapGestures { offset ->
                        selected = indexForX(offset.x, size.width.toFloat(), points.size)
                    }
                }
                .pointerInput(points) {
                    detectHorizontalDragGestures { change, _ ->
                        selected = indexForX(change.position.x, size.width.toFloat(), points.size)
                    }
                }
        ) {
            val n = points.size
            val slot = size.width / n
            val top = 12.dp.toPx()
            val bottomPad = 6.dp.toPx()
            val h = size.height - top - bottomPad
            val pts = points.mapIndexed { i, p ->
                Offset(slot * (i + 0.5f), top + h * (1f - p))
            }

            // Grid lines
            val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
            for (g in 0..2) {
                val y = top + h * g / 2f
                drawLine(
                    color = Color(0xFFE5E7EB),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dash
                )
            }

            // Curve revealed left to right
            clipRect(right = size.width * reveal.value) {
                drawPath(
                    path = smoothPath(pts, size.height),
                    brush = Brush.verticalGradient(
                        listOf(Green.copy(alpha = 0.35f), Green.copy(alpha = 0f))
                    )
                )
                drawPath(
                    path = smoothPath(pts, null),
                    color = Green,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Selected marker
            if (reveal.value > 0.9f) {
                val p = pts[safeIndex]
                drawLine(
                    color = Green.copy(alpha = 0.4f),
                    start = Offset(p.x, top),
                    end = Offset(p.x, size.height),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = dash
                )
                drawCircle(color = Color.White, radius = 8.dp.toPx(), center = p)
                drawCircle(color = Green, radius = 5.dp.toPx(), center = p)
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            labels.forEachIndexed { i, label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    fontWeight = if (i == safeIndex) FontWeight.Bold else FontWeight.Normal,
                    color = if (i == safeIndex) GreenDark else TextGray
                )
            }
        }
    }
}

private fun indexForX(x: Float, width: Float, count: Int): Int =
    ((x / width) * count).toInt().coerceIn(0, count - 1)

/** Smooth bezier path through [pts]. If [baseline] is set, closes the shape down to it (for the fill). */
private fun smoothPath(pts: List<Offset>, baseline: Float?): Path = Path().apply {
    val first = pts.first()
    if (baseline != null) {
        moveTo(first.x, baseline)
        lineTo(first.x, first.y)
    } else {
        moveTo(first.x, first.y)
    }
    for (i in 1 until pts.size) {
        val p0 = pts[i - 1]
        val p1 = pts[i]
        val cx = (p0.x + p1.x) / 2f
        cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
    }
    if (baseline != null) {
        lineTo(pts.last().x, baseline)
        close()
    }
}

// ==========================================
// STAT / INFO CARDS
// ==========================================
@Composable
private fun StatCard(
    label: String,
    value: Int,
    accent: Color,
    tint: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val animated by animateIntAsState(value, tween(900), label = "stat")

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = tint)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("$animated", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
            Text(label, fontSize = 13.sp, color = TextGray)
        }
    }
}

@Composable
private fun InfoTile(emoji: String, value: String, label: String, modifier: Modifier = Modifier) {
    ReportCard(modifier = modifier) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 26.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text(label, fontSize = 12.sp, color = TextGray)
            }
        }
    }
}

// ==========================================
// PER-MEDICINE BREAKDOWN
// ==========================================
@Composable
private fun MedicineBreakdownCard(medicines: List<MedicineAdherence>) {
    ReportCard {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("By Medicine", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(modifier = Modifier.height(14.dp))
            medicines.forEachIndexed { index, medicine ->
                MedicineRow(medicine)
                if (index != medicines.lastIndex) Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun MedicineRow(medicine: MedicineAdherence) {
    val color = adherenceColor(medicine.percent)
    var target by remember(medicine) { mutableStateOf(0f) }
    LaunchedEffect(medicine) { target = medicine.percent / 100f }
    val fraction by animateFloatAsState(target, tween(900, easing = FastOutSlowInEasing), label = "bar")

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = medicine.name.first().toString(),
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(medicine.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                Text("${medicine.taken} of ${medicine.total} doses", fontSize = 12.sp, color = TextGray)
            }
            Text("${medicine.percent}%", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(Color(0xFFEDF1F0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

// ==========================================
// INSIGHT CARD
// ==========================================
@Composable
private fun InsightCard(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.horizontalGradient(listOf(GreenDark, Green)))
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("💡", fontSize = 28.sp)
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text("Smart Insight", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text, fontSize = 13.sp, color = Color.White.copy(alpha = 0.92f), lineHeight = 18.sp)
        }
    }
}

// ==========================================
// DOWNLOAD BUTTON
// ==========================================
@Composable
private fun DownloadButton(state: DownloadState, onClick: () -> Unit) {
    val container by animateColorAsState(
        targetValue = if (state == DownloadState.DONE) GreenDark else Green,
        animationSpec = tween(300),
        label = "downloadColor"
    )

    Button(
        onClick = onClick,
        enabled = state == DownloadState.IDLE,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = Color.White,
            disabledContainerColor = container,
            disabledContentColor = Color.White
        )
    ) {
        Crossfade(targetState = state, label = "downloadContent") { s ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                when (s) {
                    DownloadState.IDLE -> {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Download Report (PDF)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    DownloadState.LOADING -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Preparing PDF…", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    DownloadState.DONE -> {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Report saved", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// SHARED HELPERS
// ==========================================
@Composable
private fun ReportCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        content = content
    )
}

/** Fades and slides content in, delayed by [index] so cards appear one after another. */
@Composable
private fun Staggered(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(450, delayMillis = index * 80)) +
                slideInVertically(tween(450, delayMillis = index * 80)) { it / 5 }
    ) {
        content()
    }
}

// ==========================================
// PREVIEW
// ==========================================
@Preview(showBackground = true, showSystemUi = true, name = "Health Reports")
@Composable
private fun HealthReportScreenPreview() {
    HealthReportScreenContent(onBackClick = {})
}