package uk.ac.cardiff.trainerhub.data.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class ReminderScheduler(
    private val context: Context,
) {
    fun update(enabled: Boolean) {
        if (enabled) {
            createChannel()
            val request = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                ReminderWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        } else {
            WorkManager.getInstance(context).cancelUniqueWork(ReminderWorker.WORK_NAME)
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel = NotificationChannel(
            ReminderWorker.CHANNEL_ID,
            "Trainer reminders",
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        channel.description = "Reminders for sessions and overdue payments"

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
