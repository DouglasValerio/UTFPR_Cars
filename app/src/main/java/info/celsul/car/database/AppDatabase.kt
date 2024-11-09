package info.celsul.car.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import info.celsul.car.database.converters.DateConverters
import info.celsul.car.database.dao.UserLocationDao
import info.celsul.car.database.model.UserLocation

@Database(entities = [UserLocation::class], version = 2, exportSchema = true)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userLocationDao(): UserLocationDao
}