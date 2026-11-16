package com.nutrirotate.app.engine

import com.nutrirotate.app.data.FoodDatabase
import com.nutrirotate.app.model.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object RotationEngine {
    val EPOCH_DATE: LocalDate = LocalDate.of(2026, 11, 16) // Week 1 starts Monday 16-11-2026

    fun currentWeekIndex(): Int {
        val days = ChronoUnit.DAYS.between(EPOCH_DATE, LocalDate.now())
        return (days / 7).toInt().coerceAtLeast(0) + 1
    }

    fun dateRangeForWeek(absWeek: Int): String {
        val start = EPOCH_DATE.plusWeeks((absWeek - 1).toLong())
        val end = start.plusDays(6)
        val fmt = DateTimeFormatter.ofPattern("d MMM")
        return "${start.format(fmt)} - ${end.format(fmt)} ${end.year}"
    }

    fun isAllowed(food: FoodItem, dietMode: DietMode, activeConditions: Set<HealthCondition>): Boolean {
        when (dietMode) {
            DietMode.VEGAN -> if (food.dietTag != FoodDietTag.VEGAN) return false
            DietMode.VEG -> if (food.dietTag == FoodDietTag.NON_VEG) return false
            DietMode.ALL -> Unit
        }
        return food.avoidIf.none { it in activeConditions }
    }

    fun getCaps(activeConditions: Set<HealthCondition>): NutrientCaps {
        return NutrientCaps(
            sodiumMax = if (HealthCondition.HIGH_BP in activeConditions) 6500 else 10500,
            satFatMax = if (HealthCondition.HIGH_CHOLESTEROL in activeConditions) 55 else 105
        )
    }

    private fun wouldOverflow(totals: Nutrients, food: FoodItem, caps: NutrientCaps): Boolean {
        val n = food.nutrients
        return (totals.satFat + n.satFat > caps.satFatMax) ||
            (totals.sodium + n.sodium > caps.sodiumMax) ||
            (totals.protein + n.protein > caps.proteinMax) ||
            (totals.fat + n.fat > caps.fatMax) ||
            (totals.iron + n.iron > caps.ironMax) ||
            (totals.calcium + n.calcium > caps.calciumMax)
    }

    private fun add(t: Nutrients, f: FoodItem): Nutrients {
        val n = f.nutrients
        return Nutrients(
            protein = t.protein + n.protein,
            carbs = t.carbs + n.carbs,
            fiber = t.fiber + n.fiber,
            fat = t.fat + n.fat,
            satFat = t.satFat + n.satFat,
            sodium = t.sodium + n.sodium,
            vitC = t.vitC + n.vitC,
            iron = t.iron + n.iron,
            calcium = t.calcium + n.calcium,
            potassium = t.potassium + n.potassium
        )
    }

    fun buildWeekPlan(
        weekNumber: Int,
        dietMode: DietMode,
        activeConditions: Set<HealthCondition>,
        pinnedIds: Set<String> = emptySet(),
        manualSwaps: Map<Pair<Int, FoodCategory>, String> = emptyMap()
    ): WeekPlan {
        val prevIds = if (weekNumber > 1) {
            generateItems(weekNumber - 1, dietMode, activeConditions, pinnedIds, manualSwaps, emptySet()).first.map { it.id }.toSet()
        } else emptySet()

        val (items, totals, caps) = generateItems(weekNumber, dietMode, activeConditions, pinnedIds, manualSwaps, prevIds)
        val start = EPOCH_DATE.plusWeeks((weekNumber - 1).toLong())
        val monthIdx = start.monthValue - 1
        val (_, mTheme) = FoodDatabase.months[monthIdx]
        val yearNum = ((weekNumber - 1) / 52) + 1
        val weekInYear = ((weekNumber - 1) % 52) + 1

        return WeekPlan(weekNumber, "Yr $yearNum • Wk $weekInYear (${dateRangeForWeek(weekNumber)})", mTheme, items, totals, caps)
    }

    private fun generateItems(
        weekNumber: Int,
        dietMode: DietMode,
        activeConditions: Set<HealthCondition>,
        pinnedIds: Set<String>,
        manualSwaps: Map<Pair<Int, FoodCategory>, String>,
        prevIds: Set<String>
    ): Triple<List<FoodItem>, Nutrients, NutrientCaps> {
        val caps = getCaps(activeConditions)
        var totals = Nutrients()
        val selected = mutableListOf<FoodItem>()
        val selectedIds = mutableSetOf<String>()

        for (pid in pinnedIds) {
            val f = FoodDatabase.foods.find { it.id == pid } ?: continue
            if (isAllowed(f, dietMode, activeConditions) && pid !in selectedIds && !wouldOverflow(totals, f, caps)) {
                selected.add(f)
                selectedIds.add(f.id)
                totals = add(totals, f)
            }
        }

        val slots = mapOf(
            FoodCategory.PROTEIN to 2,
            FoodCategory.CARBS to 2,
            FoodCategory.FATS to 1,
            FoodCategory.VITAMINS to 2,
            FoodCategory.MINERALS to 1
        )

        FoodCategory.entries.forEachIndexed { catIdx, cat ->
            val need = slots[cat] ?: 1
            val have = selected.count { it.category == cat }
            val toPick = (need - have).coerceAtLeast(0)

            var pool = FoodDatabase.foods.filter {
                it.category == cat && isAllowed(it, dietMode, activeConditions) && it.id !in selectedIds
            }
            if (pool.isEmpty()) return@forEachIndexed

            val shift = (weekNumber * 3 + catIdx * 5) % pool.size
            pool = pool.drop(shift) + pool.take(shift)
            pool = pool.sortedBy { if (it.id in prevIds) 1 else 0 }

            val swapId = manualSwaps[weekNumber to cat]
            if (swapId != null) {
                val swapped = FoodDatabase.foods.find { it.id == swapId && isAllowed(it, dietMode, activeConditions) }
                if (swapped != null && swapped.id !in selectedIds) {
                    pool = listOf(swapped) + pool.filter { it.id != swapped.id }
                }
            }

            var picked = 0
            for (candidate in pool) {
                if (picked >= toPick) break
                if (!wouldOverflow(totals, candidate, caps)) {
                    selected.add(candidate)
                    selectedIds.add(candidate.id)
                    totals = add(totals, candidate)
                    picked++
                }
            }
        }

        return Triple(selected, totals, caps)
    }
}
