package uk.ac.cardiff.trainerhub.data.reminders

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import uk.ac.cardiff.trainerhub.R
import uk.ac.cardiff.trainerhub.data.local.AppDatabase

class ReminderWorker(
    appContext: android.content.Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getInstance(applicationContext)
        val invoices = database.invoiceDao().getOverdueInvoices()

        val now = System.currentTimeMillis()
        val tomorrow = now + (24 * 60 * 60 * 1000L)
        val sessions = database.sessionDao().getSessionsBetween(now, tomorrow)
            .filter { it.status == "SCHEDULED" }

        if (invoices.isEmpty() && sessions.isEmpty()) {
            return Result.success()
        }

        val canPostNotifications = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED || android.os.Build.VERSION.SDK_INT < 33

        if (!canPostNotifications) {
            return Result.success()
        }

        val message = buildMessage(sessionCount = sessions.size, overdueCount = invoices.size)

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(applicationContext.getString(R.string.app_name))
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(1001, notification)
        return Result.success()
    }

    private fun buildMessage(sessionCount: Int, overdueCount: Int): String {
        if (sessionCount > 0 && overdueCount > 0) {
            return "$sessionCount session(s) are due soon and $overdueCount payment item(s) are overdue."
        }
        if (sessionCount > 0) {
            return "$sessionCount session(s) are due in the next 24 hours."
        }
        return "$overdueCount payment item(s) are overdue."
    }

    companion object {
        const val WORK_NAME = "trainer-hub-reminders"
        const val CHANNEL_ID = "trainer_hub_reminders"
    }
}
