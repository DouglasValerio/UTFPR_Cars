package info.celsul.car

import android.app.Application
import info.celsul.car.database.DatabaseBuilder

class Application : Application() {

    override fun onCreate() {
        super.onCreate()
        init()
    }

    private fun init() {
        DatabaseBuilder.getInstance(this)
    }
}