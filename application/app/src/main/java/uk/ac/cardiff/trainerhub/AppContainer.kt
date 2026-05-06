package uk.ac.cardiff.trainerhub

import android.content.Context
import uk.ac.cardiff.trainerhub.data.local.AppDatabase
import uk.ac.cardiff.trainerhub.data.local.DemoDataSeeder
import uk.ac.cardiff.trainerhub.data.preferences.AppPreferencesRepository
import uk.ac.cardiff.trainerhub.data.reminders.ReminderScheduler
import uk.ac.cardiff.trainerhub.data.remote.OneToOneApiClient
import uk.ac.cardiff.trainerhub.data.remote.OneToOneMobileRepository
import uk.ac.cardiff.trainerhub.data.remote.SecureSessionStore
import uk.ac.cardiff.trainerhub.data.repository.TrainerHubRepository

class AppContainer(context: Context) {
    private val applicationContext = context.applicationContext
    private val database = AppDatabase.getInstance(applicationContext)
    private val seeder = DemoDataSeeder(database)
    private val preferencesRepository = AppPreferencesRepository(applicationContext)
    private val sessionStore = SecureSessionStore(applicationContext)
    private val apiClient = OneToOneApiClient(sessionStore)

    val repository = TrainerHubRepository(
        database = database,
        seeder = seeder,
        preferencesRepository = preferencesRepository,
    )

    val reminderScheduler = ReminderScheduler(applicationContext)

    val mobileRepository = OneToOneMobileRepository(
        apiClient = apiClient,
        sessionStore = sessionStore,
    )
}
