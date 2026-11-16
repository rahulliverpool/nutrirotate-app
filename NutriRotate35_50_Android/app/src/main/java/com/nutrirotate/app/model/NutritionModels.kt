package com.nutrirotate.app.model

enum class FoodCategory(val label: String, val emoji: String) {
    PROTEIN("Protein", "💪"),
    CARBS("Complex Carbs", "🌾"),
    FATS("Healthy Fats", "🥥"),
    VITAMINS("Vitamins & Greens", "🥬"),
    MINERALS("Minerals & Gut", "🦴")
}

enum class DietMode(val label: String) {
    ALL("All Foods"),
    VEG("Veg"),
    VEGAN("Vegan")
}

enum class FoodDietTag {
    NON_VEG, EGG, VEG, VEGAN
}

enum class HealthCondition(val label: String, val emoji: String, val description: String) {
    ASTHMA("Asthma", "🫁", "Avoids sulfite-rich raisins, fermented pickles & trigger fish"),
    HIGH_BP("High Blood Pressure", "❤️", "Drops weekly Sodium cap to 6,500mg & blocks salty/pickled items"),
    HIGH_CHOLESTEROL("High Cholesterol", "🩸", "Caps Saturated Fat at 55g/wk & excludes egg yolks, ghee & paneer"),
    THYROID("Thyroid Support", "🦋", "Prioritizes selenium, zinc & cooked Kerala greens"),
    EGG_ALLERGY("Egg Allergy", "🚫🥚", "Strictly excludes all eggs & egg-based items"),
    MILK_ALLERGY("Milk / Dairy Allergy", "🚫🥛", "Excludes milk, curd, paneer & ghee; boosts ragi/sesame calcium")
}

data class Nutrients(
    val protein: Int = 0,
    val carbs: Int = 0,
    val fiber: Int = 0,
    val fat: Int = 0,
    val satFat: Int = 0,
    val sodium: Int = 0,
    val vitC: Int = 0,
    val iron: Int = 0,
    val calcium: Int = 0,
    val potassium: Int = 0
)

data class FoodItem(
    val id: String,
    val name: String,
    val emoji: String,
    val category: FoodCategory,
    val dietTag: FoodDietTag,
    val qty: Int,
    val unit: String,
    val nutrients: Nutrients,
    val avoidIf: Set<HealthCondition> = emptySet(),
    val synergy: String
) {
    fun scaledPortion(personCount: Int): String = "${qty * personCount}$unit"
}

data class NutrientCaps(
    val proteinMax: Int = 580,
    val carbsMax: Int = 1450,
    val fiberMax: Int = 290,
    val fatMax: Int = 460,
    val satFatMax: Int = 105,
    val sodiumMax: Int = 10500,
    val vitCMax: Int = 1400,
    val ironMax: Int = 140,
    val calciumMax: Int = 10500,
    val potassiumMax: Int = 32000
)

data class WeekPlan(
    val weekNumber: Int,
    val monthName: String,
    val monthTheme: String,
    val items: List<FoodItem>,
    val totals: Nutrients,
    val caps: NutrientCaps
)
