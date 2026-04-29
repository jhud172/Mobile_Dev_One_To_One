package uk.ac.cardiff.trainerhub

import android.app.Application

class TrainerHubApplication : Application() {
    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
