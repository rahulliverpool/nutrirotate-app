package com.nutrirotate.app.ui

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutrirotate.app.data.FoodDatabase
import com.nutrirotate.app.engine.RotationEngine
import com.nutrirotate.app.model.*
import com.nutrirotate.app.ui.theme.*
import kotlin.math.min

private val SurfaceElev = Color(0xFF182233)
private val InnerDark = Color(0xFF0D131D)
private val EmeraldBright = Color(0xFF10B981)
private val SkyAccent = Color(0xFF38BDF8)

@Composable
fun NutriRotateApp() {
    var currentWeek by remember { mutableIntStateOf(RotationEngine.currentWeekIndex()) }
    var activeTab by remember { mutableIntStateOf(0) }
    var personCount by remember { mutableIntStateOf(1) }
    var dietMode by remember { mutableStateOf(DietMode.ALL) }
    var activeConditions by remember { mutableStateOf(setOf<HealthCondition>()) }
    var pinnedIds by remember { mutableStateOf(setOf<String>()) }
    var boughtKeys by remember { mutableStateOf(setOf<String>()) }
    var expandedIds by remember { mutableStateOf(setOf<String>()) }
    var manualSwaps by remember { mutableStateOf(mapOf<Pair<Int, FoodCategory>, String>()) }
    var saturdayAlert by remember { mutableStateOf(true) }
    var dailyNudge by remember { mutableStateOf(true) }

    val plan = remember(currentWeek, dietMode, activeConditions, pinnedIds, manualSwaps) {
        RotationEngine.buildWeekPlan(currentWeek, dietMode, activeConditions, pinnedIds, manualSwaps)
    }

    val yearNum = ((currentWeek - 1) / 52) + 1
    val weekInYear = ((currentWeek - 1) % 52) + 1
    val dateRange = RotationEngine.dateRangeForWeek(currentWeek)

    Scaffold(
        containerColor = BgDark,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgDark)
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                // Top Header Row (no squished Material chips)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(EmeraldBright.copy(alpha = 0.16f))
                                .border(1.dp, EmeraldBright.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🥗", fontSize = 18.sp)
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("NutriRotate ", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("35-50", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EmeraldAccent)
                            }
                            Text("Kerala & India • Per-Person", fontSize = 10.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Compact 1 / 2 / 3 Person Pill Switcher
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceElev)
                                .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                                .padding(horizontal = 4.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text("👥", fontSize = 11.sp, modifier = Modifier.padding(horizontal = 2.dp))
                            listOf(1, 2, 3).forEach { count ->
                                val selected = personCount == count
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(if (selected) EmeraldBright else Color.Transparent)
                                        .clickable { personCount = count }
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "$count",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Color(0xFF052E16) else TextMuted
                                    )
                                }
                            }
                        }

                        // Compact Guard Badge Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceElev)
                                .border(1.dp, if (activeConditions.isNotEmpty()) EmeraldBright else CardBorder, RoundedCornerShape(10.dp))
                                .clickable { activeTab = 3 }
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🛡️ ${activeConditions.size}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Week Selector Bar
                Surface(
                    color = CardDark,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(SurfaceElev)
                                .clickable { if (currentWeek > 1) currentWeek-- },
                            contentAlignment = Alignment.Center
                        ) { Text("◀", fontSize = 11.sp, color = TextPrimary) }

                        Column(
                            modifier = Modifier.weight(1f).padding(horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Yr $yearNum • Wk $weekInYear", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(EmeraldBright.copy(alpha = 0.16f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(dateRange, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = EmeraldAccent, maxLines = 1)
                                }
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(plan.monthTheme, fontSize = 10.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceElev)
                                .clickable { currentWeek = RotationEngine.currentWeekIndex() }
                                .padding(horizontal = 7.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) { Text("Today", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldAccent) }

                        Spacer(Modifier.width(4.dp))

                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(SurfaceElev)
                                .clickable { currentWeek++ },
                            contentAlignment = Alignment.Center
                        ) { Text("▶", fontSize = 11.sp, color = TextPrimary) }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF0D121C)) {
                val tabs = listOf("🧺" to "This Week", "📅" to "52-Wk Plan", "🛒" to "Shop List", "🛡️" to "Health")
                tabs.forEachIndexed { idx, (emoji, label) ->
                    NavigationBarItem(
                        selected = activeTab == idx,
                        onClick = { activeTab = idx },
                        icon = { Text(emoji, fontSize = 18.sp) },
                        label = { Text(label, fontSize = 10.sp, maxLines = 1) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (activeTab) {
                0 -> WeekBasketView(
                    plan = plan,
                    personCount = personCount,
                    pinnedIds = pinnedIds,
                    boughtKeys = boughtKeys,
                    expandedIds = expandedIds,
                    onToggleExpand = { id ->
                        expandedIds = if (id in expandedIds) expandedIds - id else expandedIds + id
                    },
                    onTogglePin = { id ->
                        pinnedIds = if (id in pinnedIds) pinnedIds - id else pinnedIds + id
                    },
                    onToggleBuy = { id ->
                        val k = "${plan.weekNumber}_$id"
                        boughtKeys = if (k in boughtKeys) boughtKeys - k else boughtKeys + k
                    },
                    onSwap = { item ->
                        val currentIds = plan.items.map { it.id }.toSet()
                        val candidates = FoodDatabase.foods.filter {
                            it.category == item.category && it.id !in currentIds &&
                                RotationEngine.isAllowed(it, dietMode, activeConditions)
                        }
                        if (candidates.isNotEmpty()) {
                            manualSwaps = manualSwaps + ((plan.weekNumber to item.category) to candidates.random().id)
                        }
                    }
                )
                1 -> YearPlanView(currentWeek, dietMode, activeConditions, pinnedIds, manualSwaps) { w ->
                    currentWeek = w
                    activeTab = 0
                }
                2 -> GroceryListView(plan, personCount, boughtKeys) { id ->
                    val k = "${plan.weekNumber}_$id"
                    boughtKeys = if (k in boughtKeys) boughtKeys - k else boughtKeys + k
                }
                3 -> SafetyView(
                    dietMode = dietMode,
                    onDietChange = { dietMode = it },
                    activeConditions = activeConditions,
                    onToggleCondition = { c ->
                        activeConditions = if (c in activeConditions) activeConditions - c else activeConditions + c
                    },
                    saturdayAlert = saturdayAlert,
                    onToggleSaturday = { saturdayAlert = !saturdayAlert },
                    dailyNudge = dailyNudge,
                    onToggleDaily = { dailyNudge = !dailyNudge }
                )
            }
        }
    }
}

private data class MetricCell(val label: String, val value: Int, val cap: Int, val unit: String, val color: Color, val isLimit: Boolean = false)

@Composable
fun WeekBasketView(
    plan: WeekPlan,
    personCount: Int,
    pinnedIds: Set<String>,
    boughtKeys: Set<String>,
    expandedIds: Set<String>,
    onToggleExpand: (String) -> Unit,
    onTogglePin: (String) -> Unit,
    onToggleBuy: (String) -> Unit,
    onSwap: (FoodItem) -> Unit
) {
    val metrics = listOf(
        MetricCell("Protein", plan.totals.protein, plan.caps.proteinMax, "g", EmeraldAccent),
        MetricCell("Carbs", plan.totals.carbs, plan.caps.carbsMax, "g", Color(0xFF60A5FA)),
        MetricCell("Fiber", plan.totals.fiber, plan.caps.fiberMax, "g", Color(0xFFA78BFA)),
        MetricCell("Fat", plan.totals.fat, plan.caps.fatMax, "g", Color(0xFFFBBF24)),
        MetricCell("Vit C", plan.totals.vitC, plan.caps.vitCMax, "mg", Color(0xFF2DD4BF)),
        MetricCell("Iron", plan.totals.iron, plan.caps.ironMax, "mg", Color(0xFFF472B6)),
        MetricCell("Calcium", plan.totals.calcium, plan.caps.calciumMax, "mg", SkyAccent),
        MetricCell("Sodium", plan.totals.sodium, plan.caps.sodiumMax, "mg", Color(0xFFFB923C), true),
        MetricCell("SatFat", plan.totals.satFat, plan.caps.satFatMax, "g", Color(0xFFF87171), true)
    )

    LazyColumn(
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 3-Column Nutrition Progress Grid
        item {
            Surface(color = CardDark, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, CardBorder)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("PER-PERSON WEEKLY NUTRITION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Buy quantities scaled for $personCount ${if (personCount == 1) "person" else "persons"}", fontSize = 10.sp, color = TextMuted)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(99.dp))
                                .background(SurfaceElev)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) { Text("35-50 yrs", fontSize = 10.sp, color = TextMuted) }
                    }
                    Spacer(Modifier.height(10.dp))
                    metrics.chunked(3).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { m ->
                                val pct = min(1f, m.value.toFloat() / m.cap.coerceAtLeast(1))
                                Surface(
                                    color = InnerDark,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFF1B2434)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(m.label, fontSize = 10.sp, color = TextMuted)
                                            Text("${m.value}${m.unit}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        }
                                        Spacer(Modifier.height(5.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(4.dp)
                                                .clip(RoundedCornerShape(99.dp))
                                                .background(Color(0xFF1B2434))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(pct)
                                                    .fillMaxHeight()
                                                    .clip(RoundedCornerShape(99.dp))
                                                    .background(m.color)
                                            )
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text("${if (m.isLimit) "Cap" else "Max"}: ${m.cap}${m.unit}", fontSize = 9.sp, color = TextMuted)
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        // Categorized Food Cards
        FoodCategory.entries.forEach { cat ->
            val catItems = plan.items.filter { it.category == cat }
            if (catItems.isNotEmpty()) {
                item {
                    Text(
                        "${cat.emoji}  ${cat.label.uppercase()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldAccent,
                        modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                    )
                }
                items(catItems, key = { it.id }) { item ->
                    val bought = "${plan.weekNumber}_${item.id}" in boughtKeys
                    val expanded = item.id in expandedIds
                    val isPinned = item.id in pinnedIds

                    Surface(
                        color = CardDark,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(13.dp))
                                        .background(SurfaceElev)
                                        .border(1.dp, Color(0xFF28354D), RoundedCornerShape(13.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(item.emoji, fontSize = 24.sp)
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        item.name + if (isPinned) " 📌" else "",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (bought) TextMuted else TextPrimary,
                                        textDecoration = if (bought) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                    Text(
                                        "Buy (${personCount}p): ${item.scaledPortion(personCount)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = EmeraldAccent
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        if (item.nutrients.protein > 0) MacroPill("P ${item.nutrients.protein}g", EmeraldAccent)
                                        if (item.nutrients.carbs > 5) MacroPill("C ${item.nutrients.carbs}g", SkyAccent)
                                        if (item.nutrients.fiber > 3) MacroPill("Fib ${item.nutrients.fiber}g", Color(0xFFA78BFA))
                                    }
                                }
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SurfaceElev)
                                            .border(1.dp, Color(0xFF2A3954), RoundedCornerShape(8.dp))
                                            .clickable { onSwap(item) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) { Text("🔄 Swap", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary) }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (bought) EmeraldBright else InnerDark)
                                            .border(1.dp, if (bought) EmeraldBright else Color(0xFF243148), RoundedCornerShape(8.dp))
                                            .clickable { onToggleBuy(item.id) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            if (bought) "✓ Got" else "☐ Buy",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (bought) Color(0xFF052E16) else TextMuted
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider(color = Color(0xFF1B2537))
                            Spacer(Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Per-person nutrition & synergy", fontSize = 10.sp, color = TextMuted)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SurfaceElev)
                                        .clickable { onToggleExpand(item.id) }
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        if (expanded) "▴ Hide details" else "▾ More details",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF7DD3FC)
                                    )
                                }
                            }

                            AnimatedVisibility(visible = expanded) {
                                Surface(
                                    color = InnerDark,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, CardBorder),
                                    modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            "Iron: ${item.nutrients.iron}mg • K: ${item.nutrients.potassium}mg • Na: ${item.nutrients.sodium}mg • Fat: ${item.nutrients.fat}g (Sat: ${item.nutrients.satFat}g)",
                                            fontSize = 11.sp, color = TextMuted
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text("✨ ${item.synergy}", fontSize = 11.sp, color = Color(0xFF7DD3FC))
                                        Spacer(Modifier.height(8.dp))
                                        OutlinedButton(
                                            onClick = { onTogglePin(item.id) },
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text(if (isPinned) "📌 Unpin" else "📌 Pin at Home", fontSize = 11.sp, color = Color(0xFFFCD34D))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroPill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
fun YearPlanView(
    currentWeek: Int,
    dietMode: DietMode,
    activeConditions: Set<HealthCondition>,
    pinnedIds: Set<String>,
    manualSwaps: Map<Pair<Int, FoodCategory>, String>,
    onSelectWeek: (Int) -> Unit
) {
    var viewYear by remember(currentWeek) { mutableIntStateOf(((currentWeek - 1) / 52) + 1) }
    val startWeek = (viewYear - 1) * 52 + 1

    LazyColumn(contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Surface(color = CardDark, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, CardBorder)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Year $viewYear • 52-Week Plan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Text("Tap any week to load its rotated basket", fontSize = 11.sp, color = TextMuted)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(onClick = { if (viewYear > 1) viewYear-- }, contentPadding = PaddingValues(horizontal = 10.dp)) { Text("◀ Yr", fontSize = 11.sp) }
                        OutlinedButton(onClick = { viewYear++ }, contentPadding = PaddingValues(horizontal = 10.dp)) { Text("Yr ▶", fontSize = 11.sp) }
                    }
                }
            }
        }

        items((0 until 13).toList()) { block ->
            val blockStart = startWeek + block * 4
            Surface(color = CardDark, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, CardBorder)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Cycle ${block + 1} • Weeks ${block * 4 + 1}–${block * 4 + 4}",
                        fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldAccent
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (0 until 4).forEach { offset ->
                            val w = blockStart + offset
                            val isCurrent = w == currentWeek
                            val preview = remember(w, dietMode, activeConditions, pinnedIds, manualSwaps) {
                                RotationEngine.buildWeekPlan(w, dietMode, activeConditions, pinnedIds, manualSwaps)
                                    .items.take(3).joinToString("") { it.emoji }
                            }
                            Surface(
                                color = if (isCurrent) EmeraldBright.copy(alpha = 0.2f) else InnerDark,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (isCurrent) EmeraldAccent else CardBorder),
                                modifier = Modifier.weight(1f).clickable { onSelectWeek(w) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Wk ${((w - 1) % 52) + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Spacer(Modifier.height(3.dp))
                                    Text(preview, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GroceryListView(plan: WeekPlan, personCount: Int, boughtKeys: Set<String>, onToggle: (String) -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    fun buildShareText(): String {
        val sb = StringBuilder()
        sb.append("🥗 *NutriRotate 35-50 Grocery List*\n")
        sb.append("📅 ${plan.monthName} (${personCount}p)\n\n")
        plan.items.forEach { item ->
            val bought = "${plan.weekNumber}_${item.id}" in boughtKeys
            sb.append("${if (bought) "✅" else "☐"} ${item.emoji} ${item.name} — *${item.scaledPortion(personCount)}*\n")
        }
        return sb.toString()
    }

    LazyColumn(contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, buildShareText())
                            setPackage("com.whatsapp")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, buildShareText())
                            }, "Share Shop List"))
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("💬 WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold) }

                OutlinedButton(
                    onClick = { clipboard.setText(AnnotatedString(buildShareText())) },
                    modifier = Modifier.weight(1f)
                ) { Text("📋 Copy List", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            }
        }
        items(plan.items) { item ->
            val bought = "${plan.weekNumber}_${item.id}" in boughtKeys
            Surface(
                color = CardDark, shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth().clickable { onToggle(item.id) }
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = bought, onCheckedChange = { onToggle(item.id) })
                    Spacer(Modifier.width(4.dp))
                    Text("${item.emoji}  ${item.name}", color = if (bought) TextMuted else TextPrimary, modifier = Modifier.weight(1f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text(item.scaledPortion(personCount), color = EmeraldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SafetyView(
    dietMode: DietMode,
    onDietChange: (DietMode) -> Unit,
    activeConditions: Set<HealthCondition>,
    onToggleCondition: (HealthCondition) -> Unit,
    saturdayAlert: Boolean,
    onToggleSaturday: () -> Unit,
    dailyNudge: Boolean,
    onToggleDaily: () -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Surface(color = CardDark, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, CardBorder)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("DIET PREFERENCE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Spacer(Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DietMode.entries.forEach { mode ->
                            val selected = dietMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selected) EmeraldBright else InnerDark)
                                    .border(1.dp, if (selected) EmeraldBright else CardBorder, RoundedCornerShape(10.dp))
                                    .clickable { onDietChange(mode) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(mode.label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (selected) Color(0xFF052E16) else TextPrimary)
                            }
                        }
                    }
                }
            }
        }

        item {
            Surface(color = CardDark, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, CardBorder)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("HEALTH CONDITIONS & ALLERGIES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Text("Toggle ON to exclude trigger foods and substitute safe alternatives.", fontSize = 11.sp, color = TextMuted)
                    HealthCondition.entries.forEach { cond ->
                        val isOn = cond in activeConditions
                        Surface(
                            color = InnerDark, shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isOn) EmeraldAccent else CardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(cond.emoji, fontSize = 20.sp, modifier = Modifier.padding(end = 10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(cond.label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                    Text(cond.description, fontSize = 10.sp, color = TextMuted)
                                }
                                Switch(checked = isOn, onCheckedChange = { onToggleCondition(cond) })
                            }
                        }
                    }
                }
            }
        }

        item {
            Surface(color = CardDark, shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, CardBorder)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🔔 REMINDER SCHEDULE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                    Surface(color = InnerDark, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, CardBorder)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Saturday Weekly Basket Alert", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Text("Every Saturday evening at 6:00 PM", fontSize = 10.sp, color = TextMuted)
                            }
                            Switch(checked = saturdayAlert, onCheckedChange = { onToggleSaturday() })
                        }
                    }
                    Surface(color = InnerDark, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, CardBorder)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Daily Nutrient Synergy Nudge", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Text("Daily at 08:30 AM", fontSize = 10.sp, color = TextMuted)
                            }
                            Switch(checked = dailyNudge, onCheckedChange = { onToggleDaily() })
                        }
                    }
                }
            }
        }
    }
}
