package com.nutrirotate.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit

class WeeklyReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "NutriRotate 35-50: Weekly Basket Ready"
        val body = inputData.getString("body") ?: "Your rotated, condition-safe grocery basket for next week is ready!"
        showNotification(applicationContext, title, body)
        return Result.success()
    }

    companion object {
        private const val CHANNEL_ID = "nutrirotate_reminders"

        // Schedules weekly alert for Saturday 6:00 PM (18:00)
        fun scheduleWeeklyReminder(context: Context) {
            val now = LocalDateTime.now()
            var nextSat = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))
                .withHour(18).withMinute(0).withSecond(0)
            if (!nextSat.isAfter(now)) {
                nextSat = nextSat.plusWeeks(1)
            }
            val initialDelayMinutes = Duration.between(now, nextSat).toMinutes().coerceAtLeast(1)

            val data = workDataOf(
                "title" to "🧺 Saturday 6 PM: Weekly Basket Ready",
                "body" to "Check your rotated 35-50 groceries before Sunday shopping!"
            )
            val request = PeriodicWorkRequestBuilder<WeeklyReminderWorker>(7, TimeUnit.DAYS)
                .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
                .setInputData(data)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "weekly_basket_saturday_6pm",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun showNotification(context: Context, title: String, body: String) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(CHANNEL_ID, "Diet & Grocery Reminders", NotificationManager.IMPORTANCE_DEFAULT)
                nm.createNotificationChannel(channel)
            }
            val notif = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .build()
            nm.notify(1001, notif)
        }
    }
}
