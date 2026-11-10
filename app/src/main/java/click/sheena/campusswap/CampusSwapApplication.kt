package click.sheena.campusswap

import android.app.Application
import click.sheena.campusswap.data.CampusSwapDatabase
import click.sheena.campusswap.data.UserSession

class CampusSwapApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CampusSwapDatabase.initialize(this)
        UserSession.initialize(this)
    }
}