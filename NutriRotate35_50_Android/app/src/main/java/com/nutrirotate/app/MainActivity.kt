package com.nutrirotate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.nutrirotate.app.notifications.WeeklyReminderWorker
import com.nutrirotate.app.ui.NutriRotateApp
import com.nutrirotate.app.ui.theme.NutriRotateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WeeklyReminderWorker.scheduleWeeklyReminder(this)
        setContent {
            NutriRotateTheme {
                NutriRotateApp()
            }
        }
    }
}
